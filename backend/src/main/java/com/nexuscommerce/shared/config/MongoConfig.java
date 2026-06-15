package com.nexuscommerce.shared.config;

import java.math.BigDecimal;
import java.util.List;
import org.bson.types.Decimal128;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * Stores {@link BigDecimal} as BSON {@code Decimal128} (a numeric type) rather than
 * the Spring Data default of a String. Without this, MongoDB range queries on price
 * ({@code $gt}/{@code $lt}) compare lexicographically instead of numerically.
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                BigDecimalToDecimal128Converter.INSTANCE,
                Decimal128ToBigDecimalConverter.INSTANCE));
    }

    @WritingConverter
    enum BigDecimalToDecimal128Converter implements Converter<BigDecimal, Decimal128> {
        INSTANCE;

        @Override
        public Decimal128 convert(BigDecimal source) {
            return new Decimal128(source);
        }
    }

    @ReadingConverter
    enum Decimal128ToBigDecimalConverter implements Converter<Decimal128, BigDecimal> {
        INSTANCE;

        @Override
        public BigDecimal convert(Decimal128 source) {
            return source.bigDecimalValue();
        }
    }
}
