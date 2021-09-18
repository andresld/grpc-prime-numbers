package com.github.aldtid.grpc.prime.numbers.logging

import com.github.aldtid.grpc.prime.numbers.logging.model.ModelSyntax


object implicits {

  implicit object all extends CastingOps with ModelSyntax
  implicit object model extends ModelSyntax

}
