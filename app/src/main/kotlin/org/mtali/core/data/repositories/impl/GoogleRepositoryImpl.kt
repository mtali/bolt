/*
 * Designed and developed by 2024 mtali (Emmanuel Mtali)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mtali.core.data.repositories.impl

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode
import com.google.maps.model.Unit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.mtali.core.data.repositories.GoogleRepository
import org.mtali.core.dispatcher.BoltDispatchers.IO
import org.mtali.core.dispatcher.Dispatcher
import org.mtali.core.models.ServiceResult
import timber.log.Timber
import javax.inject.Inject

class GoogleRepositoryImpl @Inject constructor(
  @ApplicationContext context: Context,
  @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
  private val geoContext: GeoApiContext,
) : GoogleRepository {

  private var token: AutocompleteSessionToken? = null
  private val client: PlacesClient by lazy {
    Places.createClient(context)
  }

  override suspend fun getPlacesAutocomplete(query: String): ServiceResult<List<AutocompletePrediction>> = withContext(ioDispatcher) {
    if (token == null) token = AutocompleteSessionToken.newInstance()
    val request = FindAutocompletePredictionsRequest.builder()
      .setCountries(listOf("TZ"))
      .setSessionToken(token)
      .setQuery(query)
      .build()
    try {
      val task = client.findAutocompletePredictions(request).await()
      ServiceResult.Value(task.autocompletePredictions)
    } catch (e: Exception) {
      Timber.e(e)
      ServiceResult.Failure(e)
    }
  }

  override suspend fun getPlaceLatLng(placeId: String): ServiceResult<LatLng?> = withContext(ioDispatcher) {
    val placeFields = listOf(Place.Field.LAT_LNG)
    val request = FetchPlaceRequest.builder(placeId, placeFields).build()
    try {
      ServiceResult.Value(client.fetchPlace(request).await().place.latLng)
    } catch (e: Exception) {
      ServiceResult.Failure(e)
    }
  }

  override suspend fun getDirectionsRoute(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double,
  ): ServiceResult<DirectionsRoute> = withContext(ioDispatcher) {
    val result = DirectionsApi.newRequest(geoContext)
      .mode(TravelMode.DRIVING)
      .units(Unit.METRIC)
      .region("tz")
      .origin(com.google.maps.model.LatLng(originLat, originLng))
      .destination(com.google.maps.model.LatLng(destLat, destLng))
      .await()

    if (result.routes?.first() != null &&
      result.routes.isNotEmpty() &&
      result.routes.first().legs.isNotEmpty()
    ) {
      ServiceResult.Value(result.routes.first())
    } else {
      ServiceResult.Failure(Exception("Unable to retrieve address"))
    }
  }
}
