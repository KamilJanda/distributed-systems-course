package currencyService.services

import akka.actor.ActorSystem
import exchange.{ExchangeRateRequest, ExchangeRateResponse, ExchangeRateServiceGrpc}
import io.grpc.stub.StreamObserver
import scala.concurrent.duration._

import scala.concurrent.Future

class ExchangeRateService extends ExchangeRateServiceGrpc.ExchangeRateService {

  val system = ActorSystem("ExchangeRateServiceActorSystem")

  override def getExchangeRateStream(
                                      request: ExchangeRateRequest,
                                      responseObserver: StreamObserver[ExchangeRateResponse]
                                    ): Unit = {
    while (true) {
      Thread.sleep(5000)
      responseObserver.onNext(
        ExchangeRateResponse(
          ExchangeMarket.getAllRates.filter(element =>
            request.currencyType.contains(element.currency)))
      )
    }
    responseObserver.onCompleted()
  }

  override def getInitialExchangeRates(request: ExchangeRateRequest): Future[ExchangeRateResponse] = {
    Future.successful(
      ExchangeRateResponse(ExchangeMarket.getAllRates.filter(element => request.currencyType.contains(element.currency)))
    )
  }
}
