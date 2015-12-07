package eventStreaming.producer

import eventStreaming.util.MongoEventStreamUtil

/**
 * Created by prayagupd
 * on 11/28/15.
 */

object EventProducerApp {
  def main(args: Array[String]) {
    val mongo = MongoEventStreamUtil.mongoInstance
    MongoEventStreamUtil.bulkInsertVersion3(mongo)
    mongo.close()
  }
}
