package ee.taltech.mmalia

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat

object Jackson {

    val mapper = ObjectMapper()
        .apply {
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            dateFormat = StdDateFormat().withColonInTimeZone(true)
        }
}