package pl.cleankod.exchange.provider;

import pl.cleankod.exchange.core.domain.Money;
import pl.cleankod.exchange.core.gateway.CurrencyConversionService;
import pl.cleankod.exchange.provider.nbp.ExchangeRatesNbpClient;
import pl.cleankod.exchange.provider.nbp.model.RateWrapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public class CurrencyConversionNbpService implements CurrencyConversionService {
    private final ExchangeRatesNbpClient exchangeRatesNbpClient;

    public CurrencyConversionNbpService(ExchangeRatesNbpClient exchangeRatesNbpClient) {
        this.exchangeRatesNbpClient = exchangeRatesNbpClient;
    }

    @Override
    public Money convert(Money money, Currency targetCurrency) {
        // Older Code
        // RateWrapper rateWrapper = exchangeRatesNbpClient.fetch("A",
        // targetCurrency.getCurrencyCode());
        // BigDecimal midRate = rateWrapper.rates().get(0).mid();
        // BigDecimal calculatedRate = money.amount().divide(midRate,
        // RoundingMode.HALF_UP);
        // return new Money(calculatedRate, targetCurrency);

        // Implemented Solution
        if (money.currency().equals(targetCurrency)) {
            return money;
        }

        // fetching midrate for source and target currencies
        BigDecimal srcMidRate = fetchMidRate(money.currency());
        BigDecimal tgtMidRate = fetchMidRate(targetCurrency);

        // Conversion logic
        BigDecimal tgt_amount = money.amount().multiply(srcMidRate).divide(tgtMidRate, 6, RoundingMode.HALF_EVEN);

        // Rounding Logic
        int scale = Math.max(0, targetCurrency.getDefaultFractionDigits());
        BigDecimal roundedValue = tgt_amount.setScale(scale, RoundingMode.HALF_EVEN);

        return money.of(roundedValue, targetCurrency);

    }

    private BigDecimal fetchMidRate(Currency currency) {
        if ("PLN".equals(currency.getCurrencyCode())) {
            return BigDecimal.ONE; // to fix the (should return an account by ID with different currency) test case 
        }
        RateWrapper rateWrapper = exchangeRatesNbpClient.fetch("A", currency.getCurrencyCode());
        return rateWrapper.rates().get(0).mid();
    }
}
