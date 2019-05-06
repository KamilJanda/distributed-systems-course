package currencyService.services

import exchange.{ExchangeRateRequest, ExchangeRateResponse, ExchangeRateServiceGrpc}
import io.grpc.stub.StreamObserver

import scala.concurrent.Future

class ExchangeRateService extends ExchangeRateServiceGrpc.ExchangeRateService {
  override def getExchangeRateStream(
                                      request: ExchangeRateRequest,
                                      responseObserver: StreamObserver[ExchangeRateResponse]
                                    ): Unit = {
    while (true) {
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
