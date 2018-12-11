package io.infinite.pigeon

import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.CurrencyCode
import org.junit.Test

class AppTest {

    @Test
    void test() {
        System.out.println(CountryCode.getByCode("")?.getNumeric()?.toString()?.padLeft(3, "0"))
        System.out.println(CountryCode.getByCode(null)?.getNumeric()?.toString()?.padLeft(3, "0"))
        System.out.println(CountryCode.getByCode("US")?.getNumeric()?.toString()?.padLeft(3, "0"))
        System.out.println(CountryCode.getByCode("USA")?.getNumeric()?.toString()?.padLeft(3, "0"))
        System.out.println(CountryCode.getByCode("YZX")?.getNumeric()?.toString()?.padLeft(3, "0"))
        System.out.println(CountryCode.getByCode("702")?.getNumeric()?.toString()?.padLeft(3, "0"))
        System.out.println(CurrencyCode.getByCode("")?.getNumeric()?.toString()?.padLeft(3, "0"))
        System.out.println(CurrencyCode.getByCode(null)?.getNumeric()?.toString()?.padLeft(3, "0"))
        System.out.println(CurrencyCode.getByCode("US")?.getNumeric()?.toString()?.padLeft(3, "0"))
        System.out.println(CurrencyCode.getByCode("USD")?.getNumeric()?.toString()?.padLeft(3, "0"))
        System.out.println(CurrencyCode.getByCode("YZX")?.getNumeric()?.toString()?.padLeft(3, "0"))
        System.out.println(CurrencyCode.getByCode("840")?.getNumeric()?.toString()?.padLeft(3, "0"))
    }

}
