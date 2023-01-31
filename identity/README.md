# Falu Android Identity Verification

Falu's Android Identity Verification SDK provides a simple and easy-to-use PreBuild UI to help you
identity users on your android application.
The PreBuild UI enables you to collect a user's identity documents (ID, Passport, Driving Licence)
and
ensure their validity.

# Getting Started

## Usage

### Initialize Identity Verification View

Create an instance `FaluIdentityVerificationView` since its the entry point to SDK

**Note** On this step you need to provide the `Uri` to your workspace logo or an alternative logo
that will be used for branding.

```kotlin

val verificationView = FaluIdentityVerificationView.create(
    fragment = this, // activity or fragment context
    logo = Uri.parse("https://path/to/logo-no-text.jpg"),
    callback = callback
)
```

### Open Identity Verification View

The [temporary key](https://docs.falu.io/guides/keys) is mandatory. Failing to provide it will
result into an `Exception` when interacting with Falu Identity.

**Note** You must to have a valid `Identity Verification` to open `FaluIdentityVerificationView`.

```kotlin
verificationView.open("idv_1234", "ftkt_1234")
```

### Handle Identity Verification Results

```kotlin
val callback = object : IdentityVerificationCallback {
    override fun onVerificationResult(result: IdentityVerificationResult) {

        when (result) {
            IdentityVerificationResult.Succeeded -> {
                TODO("Not yet implemented")
            }
            IdentityVerificationResult.Canceled -> {
                TODO("Not yet implemented")
            }
            is IdentityVerificationResult.Failed -> {
                TODO("Not yet implemented")
            }
        }
    }
}
```

# Sample Application

[identity-sample](../identity-sample) :- Shows how to capture documents to be verified by Falu.