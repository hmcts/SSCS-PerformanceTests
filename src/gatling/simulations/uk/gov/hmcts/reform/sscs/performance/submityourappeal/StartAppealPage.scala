package uk.gov.hmcts.reform.sscs.performance.submityourappeal

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.sscs.performance.simulations.checks.CsrfCheck
import uk.gov.hmcts.reform.sscs.performance.simulations.checks.CsrfCheck.{csrfParameter, csrfTemplate}
//import uk.gov.hmcts.reform.sscs.performance.simulations.checks.{CsrfCheck, CurrentPageCheck}
//import uk.gov.hmcts.reform.idam.User
import uk.gov.hmcts.reform.sscs.performance.utils._

object StartAppealPage{

  val thinktime = Environment.thinkTime
  val idamUrl = Environment.IDAMURL
  val sscsSYAURL = Environment.sscsSYAURL

  val sscsfeeder = csv("sscs_details.csv").circular
  val loginfeeder = csv("IdamUsers.csv").circular
  //def logIn(user: User)(implicit postHeaders: Map[String, String]): ChainBuilder = {
  //1

  val entry = feed(sscsfeeder).feed(loginfeeder)
    .exec(http("TX05_SSCS_Entry")
      .get("/")
      .headers(SSCSSYAHeaders.headers_0)
      .check(status.in(200,302))
      .check(regex("Which benefit is your appeal about?"))
      //.check(CsrfCheck.save)
   )
      .pause(thinktime)

  val benifitType =
    exec(http("TX06_SSCS_BenifitType")
      .post("/benefit-type")
      .headers(SSCSSYAHeaders.headers_2)
      .formParam("benefitType", "Personal Independence Payment (PIP)") //Employment and Support Allowance (ESA) Personal Independence Payment (PIP)
      .check(status.in(200,302))
      //.formParam(csrfParameter, csrfTemplate)
      .check(regex("Enter your postcode"))
      //.check(CsrfCheck.save)
  )
    .pause(thinktime)

  val postCodeCheck =
    exec(http("TX07_SSCS_PostCodeCheck")
      .post("/postcode-check")
      .headers(SSCSSYAHeaders.headers_2)
      .formParam("postcode", "TS1 1ST")
      .check(status.in(200,302))
      //.formParam(csrfParameter, csrfTemplate)
      .check(regex("Your appeal will be decided by an independent tribunal"))
      //.check(CsrfCheck.save)
  )
    .pause(thinktime)

  val areYouAnAppointee =
    exec(http("TX08_SSCS_AreYouAnAppointee")
      .post("/are-you-an-appointee")
      .formParam("isAppointee", "no")
      .check(status.in(200,302))
      //.formParam(csrfParameter, csrfTemplate)
      .check(regex("Enter your name"))
      //.check(CsrfCheck.save)
  )
    .pause(thinktime)

//4
  val independance_beforelogin=
    exec(http("independance_beforelogin")
      .post("/independence")
      .headers(SSCSSYAHeaders.headers_2)
      .check(status.in(200,302))
      //.check(CsrfCheck.save)
      .check(regex("Do you want to be able to save this appeal later?"))
  )
    .pause(thinktime)

  val independance_postlogin=
    exec(http("independance_postlogin")
      .post("/independence")
      .check(status.in(200,302))
      //.check(CsrfCheck.save)
      //.check(regex("Do you want to be able to save this appeal later?"))
    )
      .pause(thinktime)

val savelater=
    exec(http("create account")
      .post("/create-account")
      .headers(SSCSSYAHeaders.headers_2)
      .formParam("createAccount", "yes")
      .check(CsrfCheck.save)
      .check(regex("state=(.*)&scope=").saveAs("stateId"))
      .check(status.in(200,302)))

  //contonue to save later

  val login=
    exec(http("request_141")
      .post(idamUrl + "/login?client_id=sscs&redirect_uri=" + sscsSYAURL + "%2Fauthenticated&response_type=code&state=${stateId}")
      //.headers(headers_196)
      .formParam("username", "${idamUser}") //${email}@mailinator.com
      .formParam("password", "Pass19word") //Pass19word
      .formParam("save", "Sign in")
      .formParam("selfRegistrationEnabled", "true")
      .formParam(csrfParameter, csrfTemplate)
      .check(status.in(200,302)))

}
