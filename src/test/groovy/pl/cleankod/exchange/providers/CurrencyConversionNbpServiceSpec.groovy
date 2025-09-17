package pl.cleankod.exchange.provider

import pl.cleankod.exchange.core.domain.Money
import pl.cleankod.exchange.provider.nbp.ExchangeRatesNbpClient
import pl.cleankod.exchange.provider.nbp.model.Rate
import pl.cleankod.exchange.provider.nbp.model.RateWrapper
import spock.lang.Specification

import java.math.BigDecimal
import java.util.Currency

class CurrencyConversionNbpServiceSpec extends Specification {

    def "should convert EUR to USD using cross rate and proper rounding"() {
        given:
        // Mocking NBP client to return predictable rates
        def client = Mock(ExchangeRatesNbpClient)
        client.fetch("A", "EUR") >> new RateWrapper("A", "euro", "EUR",
                [new Rate("1", "2025-01-01", new BigDecimal("4.0"))])
        client.fetch("A", "USD") >> new RateWrapper("A", "usd", "USD",
                [new Rate("1", "2025-01-01", new BigDecimal("3.0"))])

        def service = new CurrencyConversionNbpService(client)

        when:
        def result = service.convert(Money.of(new BigDecimal("10.00"), Currency.getInstance("EUR")),
                                     Currency.getInstance("USD"))

        then:
        // expected: 10 * 4.0 / 3.0 = 13.33 USD (rounded HALF_EVEN to 2 decimals)
        result.amount() == new BigDecimal("13.33")
        result.currency() == Currency.getInstance("USD")
    }

    def "should return same amount if currency matches"() {
        given:
        def client = Mock(ExchangeRatesNbpClient)
        def service = new CurrencyConversionNbpService(client)

        when:
        def money = Money.of(new BigDecimal("5.00"), Currency.getInstance("EUR"))
        def result = service.convert(money, Currency.getInstance("EUR"))

        then:
        result.is(money)
    }
}