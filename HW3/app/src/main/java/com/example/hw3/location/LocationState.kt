package com.example.hw3.location

sealed class LocationState {
    object Hint : LocationState()
    object PermissionDenied : LocationState()
    object Loading : LocationState()
    object Unavailable : LocationState()
    data class Result(val latitude: Double, val longitude: Double) : LocationState()
}