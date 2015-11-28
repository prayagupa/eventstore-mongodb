package eventStreaming.util;

/**
 * Created by prayagupd
 * on 11/24/15.
 */

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// Java


public final class CommandQueryRS {

    public static final String EVENTS_DB = "events-db";
    public static final String EVENTS_STREAM = "CommandQueryRS";
    public static final int SIZE_IN_BYTES = 20 * 971 * 520;
    public static final int MAX_DOCUMENTS = 1000;
    public static final int NO_DOCUMENTS = MAX_DOCUMENTS;
    public static final String TIMESTAMP_FIELD = "timestampIndex";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String MESSAGE = "message";

    public static void main(final String[] pArgs) throws Exception {
        final MongoClient mongo = new MongoClient(new MongoClientURI("mongodb://127.0.0.1:27017"));

        mongo.dropDatabase(EVENTS_DB);
        eventDatabase(mongo).createCollection(EVENTS_STREAM,
                new CreateCollectionOptions().capped(true).sizeInBytes(SIZE_IN_BYTES).maxDocuments(MAX_DOCUMENTS));

        final AtomicBoolean consumerThreadStatus = new AtomicBoolean(true);
        final AtomicBoolean producerThreadStatus = new AtomicBoolean(true);

        final AtomicLong producerCounter = new AtomicLong(0);
        final AtomicLong consumerCounter = new AtomicLong(0);

        final ArrayList<Thread> producerThreads = new ArrayList<Thread>();
        final ArrayList<Thread> consumerThreads = new ArrayList<Thread>();

        for (int idx = 0; idx < 2; idx++) {
            final Thread producerThread = new Thread(new EventProducer(mongo, producerThreadStatus, producerCounter));
            producerThread.start();
//
            final Thread consumerThread = new Thread(new EventConsumer(mongo, consumerThreadStatus, consumerCounter));
            consumerThread.start();

            producerThreads.add(producerThread);
            consumerThreads.add(consumerThread);
        }

        // Run for five minutes
        Thread.sleep(10 * 1000);
        producerThreadStatus.set(false);
        Thread.sleep(2 * 1000);

        for (final Thread producerThread : producerThreads) {
            producerThread.interrupt();
        }

        System.out.println("----- producer count: " + producerCounter.get());
        System.out.println("----- consumer offset: " + consumerCounter.get());
    }

    private static MongoDatabase eventDatabase(MongoClient mongo) {
        return mongo.getDatabase(EVENTS_DB);
    }

    /**
     * The thread that is reading from the capped collection.
     */
    private static class EventConsumer implements Runnable {

        private final MongoClient MONGO;
        private final AtomicBoolean RUNNING;
        private final AtomicLong COUNTER;

        private EventConsumer(final MongoClient mongoClient, final AtomicBoolean running, final AtomicLong counter) {
            MONGO = mongoClient;
            RUNNING = running;
            COUNTER = counter;
        }

        @Override
        public void run() {
            final HashSet<ObjectId> readIds = new HashSet<ObjectId>();
            long lastTimestamp = 0;
            String lastId = "";

            while (RUNNING.get()) {
                try {
                    //MONGO.getDatabase(EVENTS_DB);
                    final DBCursor tailableCursor = createTailableCursor(lastTimestamp);
                    try {
                        //com.mongodb.MongoInterruptedException: Interrupted acquiring a permit to retrieve an item from the pool
                        while (tailableCursor.hasNext() && RUNNING.get()) {
                            final BasicDBObject doc = (BasicDBObject) tailableCursor.next();
                            final ObjectId docId = doc.getObjectId("_id");
                            lastTimestamp = doc.getLong(TIMESTAMP_FIELD);
                            lastId = doc.getString("_id");

                            System.out.println("#0 (for doc id " + docId + ") -> " + COUNTER.get() + " -> " + lastId);
                            if (readIds.contains(docId)) {
                                System.out.println("------ duplicate id found: " + docId);
                            }
                            readIds.add(docId);
                            COUNTER.incrementAndGet();
                        }
                    } catch (Exception e) {
                        System.out.println("#1 " + COUNTER.get() + " => " + e.getMessage());
                    } finally {
                        try {
                            if (tailableCursor != null) {
                                tailableCursor.close();
                            }
                        } catch (final Throwable t) {
                            System.out.println("#2" + t.getMessage());
                            //MONGO.getDatabase(EVENTS_DB);
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (final InterruptedException ie) {
                        break;
                    }
                } catch (final Throwable t) {
                    System.out.println("#3 "+t.getMessage());
                }
            }
        }

        private DBCursor createTailableCursor(final long lastId) {
            final DBCollection collection = MONGO.getDB(EVENTS_DB).getCollection(EVENTS_STREAM);

            if (lastId == 0) {
                return collection.find().sort(new BasicDBObject("$natural", 1))
                        .addOption(Bytes.QUERYOPTION_TAILABLE).addOption(Bytes.QUERYOPTION_AWAITDATA);
            }
            final BasicDBObject query = new BasicDBObject(TIMESTAMP_FIELD, new BasicDBObject("$gt", lastId));
            return collection.find(query).sort(new BasicDBObject("$natural", 1))
                    .addOption(Bytes.QUERYOPTION_TAILABLE).addOption(Bytes.QUERYOPTION_AWAITDATA);
        }
    }

    /**
     * db.EventsStream.insert({TIMESTAMP_FIELD: NumberLong(1448430930244), message: NumberLong(229012), messageType: "download"})
     * The thread that is writing to the capped collection.
     */
    private static class EventProducer implements Runnable {
        private final MongoClient MONGO;
        private final AtomicBoolean RUNNING;
        private final AtomicLong COUNTER;

        private EventProducer(final MongoClient mongoClient, final AtomicBoolean running, final AtomicLong counter) {
            MONGO = mongoClient;
            RUNNING = running;
            COUNTER = counter;
        }

        @Override
        public void run() {
            while (RUNNING.get()) {
                final ObjectId docId = ObjectId.get();
                final BasicDBObject doc = new BasicDBObject("_id", docId);
                final long count = COUNTER.incrementAndGet();
                doc.put(TIMESTAMP_FIELD, System.currentTimeMillis());
                doc.put(MESSAGE_TYPE, "download");
                doc.put(MESSAGE, count);
                MONGO.getDB(EVENTS_DB).getCollection(EVENTS_STREAM).insert(doc);
            }
        }
    }

    public boolean bulkInsertVersion2(MongoClient mongo) {
        //1
        DBCollection collection = mongo.getDB(EVENTS_DB).getCollection(EVENTS_STREAM);
        BulkWriteOperation bulk = collection.initializeOrderedBulkOperation();
        for (int  i = 0; i < MAX_DOCUMENTS; i++) {
            final DBObject doc = new BasicDBObject();
            doc.put(TIMESTAMP_FIELD, System.currentTimeMillis());
            doc.put(MESSAGE_TYPE, "download");
            doc.put(MESSAGE, 12);
            bulk.insert(doc);
        }
        BulkWriteResult result = bulk.execute();
        return result.isAcknowledged();
    }

    public void bulkInsertVersion3(MongoClient mongo) {

        //1
        final Document doc = new Document();
        doc.put(TIMESTAMP_FIELD, System.currentTimeMillis());
        doc.put(MESSAGE_TYPE, "download");
        doc.put(MESSAGE, 12);

        List<Document> dbObjects = new ArrayList<Document>() {{
           add(doc);
        }};
        MongoCollection<Document> collection_ = eventCollection(mongo);
        collection_.insertMany(dbObjects);
    }

    private MongoCollection<Document> eventCollection(MongoClient mongo) {
        return eventDatabase(mongo).getCollection(EVENTS_STREAM);
    }
}

