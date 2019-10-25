package com.github.kindrat.presidio

import types.Common.FieldTypes
import types.Template
import types.Template.FieldTypeTransformation
import types.Template.ReplaceValue

object Builders {
    fun replacement(name: String, replacement: String): FieldTypeTransformation {
        return FieldTypeTransformation.newBuilder()
            .addFields(field(name))
            .setTransformation(replacement(replacement))
            .build()
    }

    fun field(name: String): FieldTypes {
        return FieldTypes.newBuilder().setName(name).build()
    }

    fun replacement(replacement: String): Template.Transformation {
        return Template.Transformation.newBuilder().setReplaceValue(replaceValue(replacement)).build()
    }

    fun replaceValue(text: String): ReplaceValue {
        return ReplaceValue.newBuilder().setNewValue(text).build()
    }
}