package ee.taltech.mmalia.db

import ee.taltech.mmalia.model.SpeedRange
import io.objectbox.converter.PropertyConverter

class IntRangeConverter :
    PropertyConverter<IntRange, String> {

    override fun convertToDatabaseValue(entityProperty: IntRange) =
        "${entityProperty.first}..${entityProperty.last}"

    override fun convertToEntityProperty(databaseValue: String): IntRange {
        return databaseValue.split("..").run {
            this[0].toInt()..this[1].toInt()
        }
    }
}

class SpeedRangeConverter :
    PropertyConverter<SpeedRange, String> {

    override fun convertToDatabaseValue(entityProperty: SpeedRange) =
        "${entityProperty.min}..${entityProperty.max}"

    override fun convertToEntityProperty(databaseValue: String): SpeedRange {
        return databaseValue.split("..").run {
            SpeedRange(this[0].toInt()..this[1].toInt())
        }
    }
}