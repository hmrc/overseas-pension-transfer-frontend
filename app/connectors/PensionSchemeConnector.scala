package connectors

import com.google.inject.Inject
import config.FrontendAppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import java.net.URL
import scala.concurrent.ExecutionContext


class PensionSchemeConnector @Inject()(
                                        appConfig: FrontendAppConfig,
                                        http: HttpClientV2
                                      )(implicit ec: ExecutionContext) {

  def getScheme(srn: String)(implicit hc: HeaderCarrier) = {
    val url: URL = url"${appConfig.pensionSchemeService}/scheme"

    http.get(url)
      .execute[HttpResponse]
  }

}
