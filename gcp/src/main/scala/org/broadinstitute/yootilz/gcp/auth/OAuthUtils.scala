package org.broadinstitute.yootilz.gcp.auth

import java.time.Instant

import better.files.File
import com.google.auth.oauth2.{AccessToken, GoogleCredentials, ServiceAccountCredentials}
import sttp.client.Request

import scala.util.Try


object OAuthUtils {

  def getApplicationDefaultCredentials: GoogleCredentials = {
    GoogleCredentials.getApplicationDefault
  }

  def readServiceAccountCredentials(file: File): Try[ServiceAccountCredentials] = {
    Try(ServiceAccountCredentials.fromStream(file.newFileInputStream))
  }

  def withScopes(credentials: GoogleCredentials, scopes: String*): GoogleCredentials = {
    credentials.createScoped(scopes: _*)
  }

  def accessTokenOpt(credentials: GoogleCredentials, minSecondsRemaining: Int = 1): Option[AccessToken] = {
    Option(credentials.getAccessToken).filter { token =>
      token.getExpirationTime.toInstant.isBefore(Instant.now.minusSeconds(minSecondsRemaining))
    } orElse {
      credentials.refresh()
      Option(credentials.getAccessToken)
    }
  }

  def addAccessToken[T, S](request: Request[T, S], token: AccessToken): Request[T, S] = {
    request.auth.bearer(token.getTokenValue)
  }

  def addAccessToken[T, S](request: Request[T, S], credentials: GoogleCredentials): Request[T, S] = {
    addAccessToken(request, accessTokenOpt(credentials).get)
  }
}
