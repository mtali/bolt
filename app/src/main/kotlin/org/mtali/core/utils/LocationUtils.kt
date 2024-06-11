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

import android.location.Location
import com.google.maps.model.LatLng

private const val IMPOSSIBLE_LAT_LON = 999.0

val dummyLatLng = LatLng(IMPOSSIBLE_LAT_LON, IMPOSSIBLE_LAT_LON)

fun LatLng.isDummy() = (lat == IMPOSSIBLE_LAT_LON || lng == IMPOSSIBLE_LAT_LON)

fun Location.toLatLng() = LatLng(latitude, longitude)
