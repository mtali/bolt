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
package org.mtali.core.utils

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState

fun getLatLngBounds(vararg positions: LatLng): LatLngBounds {
  val boundsBuilder = LatLngBounds.builder()
  positions.forEach { boundsBuilder.include(it) }
  return boundsBuilder.build()
}

suspend fun CameraPositionState.animateToBounds(pos1: LatLng, pos2: LatLng, padding: Int = 100) {
  animate(CameraUpdateFactory.newLatLngBounds(getLatLngBounds(pos1, pos2), padding))
}

suspend fun CameraPositionState.animateToBounds(
  lat1: Double,
  lng1: Double,
  lat2: Double,
  lng2: Double,
) {
  animateToBounds(LatLng(lat1, lng1), LatLng(lat2, lng2))
}
