package com.davi.dev.gnixia.domain.model

/** Thrown when the device can't run the on-device model (e.g. 32-bit only CPUs). */
class UnsupportedDeviceException : Exception("Device not supported by the on-device model")
