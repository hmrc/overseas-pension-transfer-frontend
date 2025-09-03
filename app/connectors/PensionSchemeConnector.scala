package connectors

import com.google.inject.Inject
import config.FrontendAppConfig
import models.authentication.PsaId
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.net.URL
import scala.concurrent.ExecutionContext


class PensionSchemeConnector @Inject()(
                                        appConfig: FrontendAppConfig,
                                        http: HttpClientV2
                                      )(implicit ec: ExecutionContext) {

  def getScheme()(implicit hc: HeaderCarrier) = {
    val url: URL = url"${appConfig.pensionSchemeService}/scheme"

    http.get(url)
      .execute
  }

  def checkAssociation(srn: String, psaId: PsaId)(implicit hc: HeaderCarrier) = {
    val url = url"${appConfig.pensionSchemeService}/register-scheme"

    http.post(url)
      .setHeader(
        "schemeReferenceNumber" -> srn,
        "psaId" -> psaId.value
      )
      .execute
  }

}
