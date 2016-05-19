Example Play application to demonstrate better validation
=========================================================

This example covers how to serialize `Scala => JSON` and deserialize `JSON => Scala` in addition to performing validation
using hand-rolled methods without the use of in built helpers (see the *More Information* section for that)

[![Build Status](https://travis-ci.org/referentiallytransparent/play-framework-validation-example.svg?branch=master)](https://travis-ci.org/referentiallytransparent/play-framework-validation-example)
[![codecov](https://codecov.io/gh/referentiallytransparent/play-framework-validation-example/branch/master/graph/badge.svg)](https://codecov.io/gh/referentiallytransparent/play-framework-validation-example)


Controllers
===========

- **PersonController**:

  Shows how to handle JSON and perform better-than-average validation

Models
======

- **Person**

  An example data class which has JSON converters defined in the companion object allowing serialization to JSON without
  additional validation and using Play framework Json.Reads filters
  See [here](http://stackoverflow.com/questions/26317186/custom-json-validation-constraints-in-play-framework-2-3-scala) for more information

- **ErrorResponse**

  Another data class that has a Play framework Json.Writes defined to allow for easy serialization to JSON and is used
  to display error messages when `POST /person` is sent incorrect JSON


More Information
================

- See [ScalaJsonCombinators](https://www.playframework.com/documentation/2.5.x/ScalaJsonCombinators) which cover using validation helpers
  - More specifically [Validation with Reads](https://www.playframework.com/documentation/2.5.x/ScalaJsonCombinators#Validation-with-Reads)
