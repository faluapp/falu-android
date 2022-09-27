<a href="https://www.falu.io">
    <img src="https://cdn.falu.io/tools/logo.png" alt="Falu Logo" title="Falu" width="120" height="120" align="right">
</a>

# Falu Android

![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/tinglesoftware/falu-android?label=gradle)
[![Falu Android](https://github.com/tinglesoftware/falu-android/actions/workflows/build-release.yml/badge.svg)](https://github.com/tinglesoftware/falu-android/actions/workflows/build-release.yml)
![Language](https://img.shields.io/badge/language-Kotlin%205.0-green.svg)

Falu's Android SDK simplifies the process of building excellent financial services into Android
applications. We expose APIs that will enable to you to make payment and handle identity verification.

## Installation

### Requirements

* Android 5.0 (API level 21) and above
* [Android Gradle Plugin] (https://developer.android.com/studio/releases/gradle-plugin) 4.2.2

### Setup

Add `falu` to your `build.gradle` dependencies.

```gradle
implementation "io.falu:falu-android:VERSION_NUMBER"
```

# Getting Started

## Setup Guide

Get started with our [Setup Guide](https://docs.falu.io/guides/developer/quickstart).

### Creating an instance of Falu

Create an instance `Falu` since its the entry point to SDK

```kotlin
val falu = Falu(this, "PUBLIC_KEY")
```

The [public key](https://docs.falu.io/guides/keys) is mandatory. Failing to provide it will result
into an `Exception` when interacting with Falu.

You can also enable logging of network operations as follows:

```kotlin
val falu = Falu(this, "PUBLIC_KEY", true)
```

**NOTE**: It is recommended to **disable** logging in production

# Features

Once you have finished the setup process, you can proceed to use the features and functionalities
offered by the SDK

## Payments

Create a `Payment` object when initiating payments from a customer. Falu supports several payment
methods including `MPESA`.
See [How to Authorize Payments](https://docs.falu.io/guides/payments/authorizations) and
[How to Accept Payments](https://docs.falu.io/guides/payments) for information.

How to initiate `MPESA` payments:

```kotlin
val mpesa = MpesaPaymentRequest(
    phone = "+254722000000",
    reference = "254722000000",
    paybill = true,
    destination = "200200"
)

val request = PaymentRequest(
    amount = 100,
    currency = "kes",
    mpesa = mpesa
)

falu.createPayment(request, callback)

// api response callbacks
private val callback = object : ApiResultCallback<Payment> {
    override fun onSuccess(result: Payment) {
        // display in UI element
    }

    override fun onError(e: Exception) {
        print(e)
    }
}
```

## Upload files

This feature allows you to upload files. See the
documentation for more information on how to
handle [files and uploads](https://docs.falu.io/guides/files)

```kotlin
val request = UploadRequest(
    file = file,
    purpose = "customer.selfie"
)

// making the request
falu.createFile(request, callbacks)

// api response callbacks
private val callbacks = object : ApiResultCallback<FaluFile> {
    override fun onSuccess(result: FaluFile) {
        // File upload succeeded
    }

    override fun onError(e: Exception) {
        // File upload failed
    }
}
```
