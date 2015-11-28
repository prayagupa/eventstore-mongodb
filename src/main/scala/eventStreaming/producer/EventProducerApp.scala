package eventStreaming.producer

import eventStreaming.util.MongoUtil

/**
 * Created by prayagupd
 * on 11/28/15.
 */

object EventProducerApp {
  def main(args: Array[String]) {
    val mongo = MongoUtil.mongoInstance
    MongoUtil.bulkInsertVersion3(mongo)
    mongo.close()
  }
}
