/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.avatica.util;

import org.junit.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static org.apache.calcite.avatica.util.DateTimeUtils.EPOCH_JULIAN;
import static org.apache.calcite.avatica.util.DateTimeUtils.addMonths;
import static org.apache.calcite.avatica.util.DateTimeUtils.dateStringToUnixDate;
import static org.apache.calcite.avatica.util.DateTimeUtils.digitCount;
import static org.apache.calcite.avatica.util.DateTimeUtils.floorDiv;
import static org.apache.calcite.avatica.util.DateTimeUtils.floorMod;
import static org.apache.calcite.avatica.util.DateTimeUtils.intervalDayTimeToString;
import static org.apache.calcite.avatica.util.DateTimeUtils.intervalYearMonthToString;
import static org.apache.calcite.avatica.util.DateTimeUtils.subtractMonths;
import static org.apache.calcite.avatica.util.DateTimeUtils.timeStringToUnixDate;
import static org.apache.calcite.avatica.util.DateTimeUtils.timestampStringToUnixDate;
import static org.apache.calcite.avatica.util.DateTimeUtils.unixDateCeil;
import static org.apache.calcite.avatica.util.DateTimeUtils.unixDateExtract;
import static org.apache.calcite.avatica.util.DateTimeUtils.unixDateFloor;
import static org.apache.calcite.avatica.util.DateTimeUtils.unixDateToString;
import static org.apache.calcite.avatica.util.DateTimeUtils.unixTimeExtract;
import static org.apache.calcite.avatica.util.DateTimeUtils.unixTimeToString;
import static org.apache.calcite.avatica.util.DateTimeUtils.unixTimestamp;
import static org.apache.calcite.avatica.util.DateTimeUtils.unixTimestampExtract;
import static org.apache.calcite.avatica.util.DateTimeUtils.unixTimestampToString;
import static org.apache.calcite.avatica.util.DateTimeUtils.ymdToJulian;
import static org.apache.calcite.avatica.util.DateTimeUtils.ymdToUnixDate;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link DateTimeUtils}.
 */
public class DateTimeUtilsTest {
  @Test public void testEasyLog10() {
    assertEquals(1, digitCount(0));
    assertEquals(1, digitCount(1));
    assertEquals(1, digitCount(9));
    assertEquals(2, digitCount(10));
    assertEquals(2, digitCount(11));
    assertEquals(2, digitCount(99));
    assertEquals(3, digitCount(100));
  }

  @Test public void testFloorDiv() {
    assertThat(floorDiv(13, 3), equalTo(4L));
    assertThat(floorDiv(12, 3), equalTo(4L));
    assertThat(floorDiv(11, 3), equalTo(3L));
    assertThat(floorDiv(-13, 3), equalTo(-5L));
    assertThat(floorDiv(-12, 3), equalTo(-4L));
    assertThat(floorDiv(-11, 3), equalTo(-4L));
    assertThat(floorDiv(0, 3), equalTo(0L));
    assertThat(floorDiv(1, 3), equalTo(0L));
    assertThat(floorDiv(-1, 3), is(-1L));
  }

  @Test public void testFloorMod() {
    assertThat(floorMod(13, 3), is(1L));
    assertThat(floorMod(12, 3), is(0L));
    assertThat(floorMod(11, 3), is(2L));
    assertThat(floorMod(-13, 3), is(2L));
    assertThat(floorMod(-12, 3), is(0L));
    assertThat(floorMod(-11, 3), is(1L));
    assertThat(floorMod(0, 3), is(0L));
    assertThat(floorMod(1, 3), is(1L));
    assertThat(floorMod(-1, 3), is(2L));
  }

  @Test public void testTimeUnitRange() {
    assertSame(TimeUnitRange.of(TimeUnit.YEAR, null), TimeUnitRange.YEAR);
    assertSame(TimeUnitRange.of(TimeUnit.YEAR, TimeUnit.MONTH), TimeUnitRange.YEAR_TO_MONTH);
    assertSame(TimeUnitRange.of(TimeUnit.MONTH, null), TimeUnitRange.MONTH);
    assertSame(TimeUnitRange.of(TimeUnit.DAY, null), TimeUnitRange.DAY);
    assertSame(TimeUnitRange.of(TimeUnit.DAY, TimeUnit.HOUR), TimeUnitRange.DAY_TO_HOUR);
    assertSame(TimeUnitRange.of(TimeUnit.DAY, TimeUnit.MINUTE), TimeUnitRange.DAY_TO_MINUTE);
    assertSame(TimeUnitRange.of(TimeUnit.DAY, TimeUnit.SECOND), TimeUnitRange.DAY_TO_SECOND);
    assertSame(TimeUnitRange.of(TimeUnit.HOUR, null), TimeUnitRange.HOUR);
    assertSame(TimeUnitRange.of(TimeUnit.HOUR, TimeUnit.MINUTE), TimeUnitRange.HOUR_TO_MINUTE);
    assertSame(TimeUnitRange.of(TimeUnit.HOUR, TimeUnit.SECOND), TimeUnitRange.HOUR_TO_SECOND);
    assertSame(TimeUnitRange.of(TimeUnit.MINUTE, null), TimeUnitRange.MINUTE);
    assertSame(TimeUnitRange.of(TimeUnit.MINUTE, TimeUnit.SECOND), TimeUnitRange.MINUTE_TO_SECOND);
    assertSame(TimeUnitRange.of(TimeUnit.SECOND, null), TimeUnitRange.SECOND);
    assertSame(TimeUnitRange.of(TimeUnit.ISOYEAR, null), TimeUnitRange.ISOYEAR);
    assertSame(TimeUnitRange.of(TimeUnit.QUARTER, null), TimeUnitRange.QUARTER);
    assertSame(TimeUnitRange.of(TimeUnit.WEEK, null), TimeUnitRange.WEEK);
    assertSame(TimeUnitRange.of(TimeUnit.MILLISECOND, null), TimeUnitRange.MILLISECOND);
    assertSame(TimeUnitRange.of(TimeUnit.MICROSECOND, null), TimeUnitRange.MICROSECOND);
    assertSame(TimeUnitRange.of(TimeUnit.NANOSECOND, null), TimeUnitRange.NANOSECOND);
    assertSame(TimeUnitRange.of(TimeUnit.DOW, null), TimeUnitRange.DOW);
    assertSame(TimeUnitRange.of(TimeUnit.ISODOW, null), TimeUnitRange.ISODOW);
    assertSame(TimeUnitRange.of(TimeUnit.DOY, null), TimeUnitRange.DOY);
    assertSame(TimeUnitRange.of(TimeUnit.EPOCH, null), TimeUnitRange.EPOCH);
    assertSame(TimeUnitRange.of(TimeUnit.DECADE, null), TimeUnitRange.DECADE);
    assertSame(TimeUnitRange.of(TimeUnit.CENTURY, null), TimeUnitRange.CENTURY);
    assertSame(TimeUnitRange.of(TimeUnit.MILLENNIUM, null), TimeUnitRange.MILLENNIUM);
  }

  @Test public void testTimeUnitMultipliers() {
    assertEquals(TimeUnit.NANOSECOND.multiplier,
        TimeUnit.MICROSECOND.multiplier.divide(BigDecimal.valueOf(1000)));
    assertEquals(TimeUnit.MICROSECOND.multiplier,
        TimeUnit.MILLISECOND.multiplier.divide(BigDecimal.valueOf(1000)));
    assertEquals(TimeUnit.MILLISECOND.multiplier,
        TimeUnit.SECOND.multiplier.divide(BigDecimal.valueOf(1000)));
    assertEquals(BigDecimal.valueOf(60),
        TimeUnit.HOUR.multiplier.divide(TimeUnit.MINUTE.multiplier));
    assertEquals(BigDecimal.valueOf(60),
        TimeUnit.MINUTE.multiplier.divide(TimeUnit.SECOND.multiplier));
    assertEquals(BigDecimal.valueOf(24),
        TimeUnit.DAY.multiplier.divide(TimeUnit.HOUR.multiplier));
    assertEquals(BigDecimal.valueOf(7),
        TimeUnit.WEEK.multiplier.divide(TimeUnit.DAY.multiplier));
    assertEquals(BigDecimal.valueOf(4),
        TimeUnit.YEAR.multiplier.divide(TimeUnit.QUARTER.multiplier));
    assertEquals(BigDecimal.valueOf(12),
        TimeUnit.YEAR.multiplier.divide(TimeUnit.MONTH.multiplier));
    assertEquals(BigDecimal.valueOf(12),
        TimeUnit.ISOYEAR.multiplier.divide(TimeUnit.MONTH.multiplier));
    assertEquals(BigDecimal.valueOf(3),
        TimeUnit.QUARTER.multiplier.divide(TimeUnit.MONTH.multiplier));
    assertEquals(BigDecimal.valueOf(10),
        TimeUnit.DECADE.multiplier.divide(TimeUnit.YEAR.multiplier));
    assertEquals(BigDecimal.valueOf(100),
        TimeUnit.CENTURY.multiplier.divide(TimeUnit.YEAR.multiplier));
    assertEquals(BigDecimal.valueOf(1000),
        TimeUnit.MILLENNIUM.multiplier.divide(TimeUnit.YEAR.multiplier));
  }

  @Test public void testUnixDateToString() {
    // Verify these using the "date" command. E.g.
    // $ date -u --date="@$(expr 10957 \* 86400)"
    // Sat Jan  1 00:00:00 UTC 2000
    assertEquals("2000-01-01", unixDateToString(10957));

    assertEquals("1970-01-01", unixDateToString(0));
    assertEquals("1970-01-02", unixDateToString(1));
    assertEquals("1971-01-01", unixDateToString(365));
    assertEquals("1972-01-01", unixDateToString(730));
    assertEquals("1972-02-28", unixDateToString(788));
    assertEquals("1972-02-29", unixDateToString(789));
    assertEquals("1972-03-01", unixDateToString(790));

    assertEquals("1969-01-01", unixDateToString(-365));
    assertEquals("2000-01-01", unixDateToString(10957));
    assertEquals("2000-02-28", unixDateToString(11015));
    assertEquals("2000-02-29", unixDateToString(11016));
    assertEquals("2000-03-01", unixDateToString(11017));
    assertEquals("1900-01-01", unixDateToString(-25567));
    assertEquals("1900-02-28", unixDateToString(-25509));
    assertEquals("1900-03-01", unixDateToString(-25508));
    assertEquals("1945-02-24", unixDateToString(-9077));
  }

  @Test public void testYmdToUnixDate() {
    assertEquals(0, ymdToUnixDate(1970, 1, 1));
    assertEquals(365, ymdToUnixDate(1971, 1, 1));
    assertEquals(-365, ymdToUnixDate(1969, 1, 1));
    assertEquals(11015, ymdToUnixDate(2000, 2, 28));
    assertEquals(11016, ymdToUnixDate(2000, 2, 29));
    assertEquals(11017, ymdToUnixDate(2000, 3, 1));
    assertEquals(-9077, ymdToUnixDate(1945, 2, 24));
    assertEquals(-25509, ymdToUnixDate(1900, 2, 28));
    assertEquals(-25508, ymdToUnixDate(1900, 3, 1));
  }

  @Test public void testDateToString() {
    checkDateString("1970-01-01", 0);
    //noinspection PointlessArithmeticExpression
    checkDateString("1971-02-03", 0 + 365 + 31 + (3 - 1));
    //noinspection PointlessArithmeticExpression
    checkDateString("1971-02-28", 0 + 365 + 31 + (28 - 1));
    //noinspection PointlessArithmeticExpression
    checkDateString("1971-03-01", 0 + 365 + 31 + 28 + (1 - 1));
    //noinspection PointlessArithmeticExpression
    checkDateString("1972-02-28", 0 + 365 * 2 + 31 + (28 - 1));
    //noinspection PointlessArithmeticExpression
    checkDateString("1972-02-29", 0 + 365 * 2 + 31 + (29 - 1));
    //noinspection PointlessArithmeticExpression
    checkDateString("1972-03-01", 0 + 365 * 2 + 31 + 29 + (1 - 1));

    final int d1900 = -(70 * 365 + 70 / 4);
    final int century = 100 * 365 + 100 / 4;
    checkDateString("1900-01-01", d1900);
    // +1 because 1800 is not a leap year
    final int d1800 = d1900 - century + 1;
    checkDateString("1800-01-01", d1800);
    final int d1700 = d1800 - century + 1;
    checkDateString("1700-01-01", d1700);
    final int d1600 = d1700 - century;
    checkDateString("1600-01-01", d1600);
    final int d1500 = d1600 - century + 1;
    checkDateString("1500-01-01", d1500); // fails, about 10 days off
  }

  private void checkDateString(String s, int d) {
    assertThat(unixDateToString(d), is(s));
    assertThat(dateStringToUnixDate(s), is(d));
  }

  @Test public void testTimeToString() {
    checkTimeString("00", "00:00:00", 0, 0);
    checkTimeString("00:00", "00:00:00", 0, 0);
    checkTimeString("00:00:00", 0, 0);
    checkTimeString("23:59:59", 0, 86400000 - 1000);
    checkTimeString("23:59:59.1", 1, 86400000 - 1000 + 100);
    checkTimeString("23:59:59.01", 2, 86400000 - 1000 + 10);
    checkTimeString("23:59:59.1234", 3, 86400000 - 1000 + 123);
    checkTimeString("23:59:59.1236", 3, 86400000 - 1000 + 124);
    checkTimeString("23:59:59.123456789012345678901234567890", 3,
        86400000 - 1000 + 123);
  }

  @Test public void testTimestampExtract() {
    // 1970-01-01 00:00:00.000
    assertThat(unixTimestampExtract(TimeUnitRange.HOUR, 0L), is(0));
    assertThat(unixTimestampExtract(TimeUnitRange.MINUTE, 0L), is(0));
    assertThat(unixTimestampExtract(TimeUnitRange.SECOND, 0L), is(0));
    // 1970-01-02 00:00:00.000
    assertThat(unixTimestampExtract(TimeUnitRange.HOUR, 86400000L), is(0));
    assertThat(unixTimestampExtract(TimeUnitRange.MINUTE, 86400000L), is(0));
    assertThat(unixTimestampExtract(TimeUnitRange.SECOND, 86400000L), is(0));
  }

  @Test public void testTimeExtract() {
    // 00:00:00.000
    assertThat(unixTimeExtract(TimeUnitRange.HOUR, 0), is(0));
    assertThat(unixTimeExtract(TimeUnitRange.MINUTE, 0), is(0));
    assertThat(unixTimeExtract(TimeUnitRange.SECOND, 0), is(0));
    // 00:59:59.999
    assertThat(unixTimeExtract(TimeUnitRange.HOUR, 3599999), is(0));
    assertThat(unixTimeExtract(TimeUnitRange.MINUTE, 3599999), is(59));
    assertThat(unixTimeExtract(TimeUnitRange.SECOND, 3599999), is(59));
    // 01:59:59.999
    assertThat(unixTimeExtract(TimeUnitRange.HOUR, 7199999), is(1));
    assertThat(unixTimeExtract(TimeUnitRange.MINUTE, 7199999), is(59));
    assertThat(unixTimeExtract(TimeUnitRange.SECOND, 7199999), is(59));
    // 01:58:59.999
    assertThat(unixTimeExtract(TimeUnitRange.HOUR, 7139999), is(1));
    assertThat(unixTimeExtract(TimeUnitRange.MINUTE, 7139999), is(58));
    assertThat(unixTimeExtract(TimeUnitRange.SECOND, 7139999), is(59));
    // 23:59:59.999
    assertThat(unixTimeExtract(TimeUnitRange.HOUR, 86399999), is(23));
    assertThat(unixTimeExtract(TimeUnitRange.MINUTE, 86399999), is(59));
    assertThat(unixTimeExtract(TimeUnitRange.SECOND, 86399999), is(59));
  }

  private void checkTimeString(String s, int p, int d) {
    checkTimeString(s, s, p, d);
  }

  private void checkTimeString(String string, String expectedString, int p, int d) {
    int digitsAfterPoint = string.indexOf('.') >= 0
        ? string.length() - string.indexOf('.') - 1
        : 0;
    if (digitsAfterPoint == p) {
      assertThat(unixTimeToString(d, p), is(expectedString));
    }
    assertThat(timeStringToUnixDate(string), is(d));
  }

  @Test public void testTimestampToString() {
    // ISO format would be "1970-01-01T00:00:00" but SQL format is different
    checkTimestampString("1970-01-01 00:00:00", 0, 0L);
    checkTimestampString("1970-02-01 23:59:59", 0, 86400000L * 32L - 1000L);
    checkTimestampString("1970-02-01 23:59:59.123", 3,
        86400000L * 32L - 1000L + 123);
    checkTimestampString("1970-02-01 23:59:59.04", 2,
        86400000L * 32L - 1000L + 40);
  }

  private void checkTimestampString(String s, int p, long d) {
    assertThat(unixTimestampToString(d, p), is(s));
    assertThat(timestampStringToUnixDate(s), is(d));
  }

  @Test public void testIntervalYearMonthToString() {
    TimeUnitRange range = TimeUnitRange.YEAR_TO_MONTH;
    assertEquals("+0-00", intervalYearMonthToString(0, range));
    assertEquals("+1-00", intervalYearMonthToString(12, range));
    assertEquals("+1-01", intervalYearMonthToString(13, range));
    assertEquals("-1-01", intervalYearMonthToString(-13, range));
  }

  @Test public void testIntervalDayTimeToString() {
    assertEquals("+0", intervalYearMonthToString(0, TimeUnitRange.YEAR));
    assertEquals("+0-00",
        intervalYearMonthToString(0, TimeUnitRange.YEAR_TO_MONTH));
    assertEquals("+0", intervalYearMonthToString(0, TimeUnitRange.MONTH));
    assertEquals("+0", intervalDayTimeToString(0, TimeUnitRange.DAY, 0));
    assertEquals("+0 00",
        intervalDayTimeToString(0, TimeUnitRange.DAY_TO_HOUR, 0));
    assertEquals("+0 00:00",
        intervalDayTimeToString(0, TimeUnitRange.DAY_TO_MINUTE, 0));
    assertEquals("+0 00:00:00",
        intervalDayTimeToString(0, TimeUnitRange.DAY_TO_SECOND, 0));
    assertEquals("+0", intervalDayTimeToString(0, TimeUnitRange.HOUR, 0));
    assertEquals("+0:00",
        intervalDayTimeToString(0, TimeUnitRange.HOUR_TO_MINUTE, 0));
    assertEquals("+0:00:00",
        intervalDayTimeToString(0, TimeUnitRange.HOUR_TO_SECOND, 0));
    assertEquals("+0",
        intervalDayTimeToString(0, TimeUnitRange.MINUTE, 0));
    assertEquals("+0:00",
        intervalDayTimeToString(0, TimeUnitRange.MINUTE_TO_SECOND, 0));
    assertEquals("+0",
        intervalDayTimeToString(0, TimeUnitRange.SECOND, 0));
  }

  @Test public void testYmdToJulian() {
    // All checked using http://aa.usno.navy.mil/data/docs/JulianDate.php.
    // We round up - if JulianDate.php gives 2451544.5, we use 2451545.
    assertThat(ymdToJulian(2014, 4, 3), is(2456751));

    // 2000 is a leap year
    assertThat(ymdToJulian(2000, 1, 1), is(2451545));
    assertThat(ymdToJulian(2000, 2, 28), is(2451603));
    assertThat(ymdToJulian(2000, 2, 29), is(2451604));
    assertThat(ymdToJulian(2000, 3, 1), is(2451605));

    assertThat(ymdToJulian(1970, 1, 1), is(2440588));
    assertThat(ymdToJulian(1970, 1, 1), is(EPOCH_JULIAN));
    assertThat(ymdToJulian(1901, 1, 1), is(2415386));

    // 1900 is not a leap year
    assertThat(ymdToJulian(1900, 10, 17), is(2415310));
    assertThat(ymdToJulian(1900, 3, 1), is(2415080));
    assertThat(ymdToJulian(1900, 2, 28), is(2415079));
    assertThat(ymdToJulian(1900, 2, 1), is(2415052));
    assertThat(ymdToJulian(1900, 1, 1), is(2415021));

    assertThat(ymdToJulian(1777, 7, 4), is(2370281));

    // 2016 is a leap year
    assertThat(ymdToJulian(2016, 2, 28), is(2457447));
    assertThat(ymdToJulian(2016, 2, 29), is(2457448));
    assertThat(ymdToJulian(2016, 3, 1), is(2457449));
  }

  @Test public void testExtract() {
    assertThat(unixDateExtract(TimeUnitRange.YEAR, 0), is(1970L));
    assertThat(unixDateExtract(TimeUnitRange.YEAR, -1), is(1969L));
    assertThat(unixDateExtract(TimeUnitRange.YEAR, 364), is(1970L));
    assertThat(unixDateExtract(TimeUnitRange.YEAR, 365), is(1971L));

    assertThat(unixDateExtract(TimeUnitRange.MONTH, 0), is(1L));
    assertThat(unixDateExtract(TimeUnitRange.MONTH, -1), is(12L));
    assertThat(unixDateExtract(TimeUnitRange.MONTH, 364), is(12L));
    assertThat(unixDateExtract(TimeUnitRange.MONTH, 365), is(1L));

    // 1969/12/31 was a Wed (4)
    assertThat(unixDateExtract(TimeUnitRange.DOW, -1), is(4L)); // wed
    assertThat(unixDateExtract(TimeUnitRange.DOW, 0), is(5L)); // thu
    assertThat(unixDateExtract(TimeUnitRange.DOW, 1), is(6L)); // fri
    assertThat(unixDateExtract(TimeUnitRange.DOW, 2), is(7L)); // sat
    assertThat(unixDateExtract(TimeUnitRange.DOW, 3), is(1L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.DOW, 365), is(6L));
    assertThat(unixDateExtract(TimeUnitRange.DOW, 366), is(7L));

    // 1969/12/31 was a Wed (4)
    assertThat(unixDateExtract(TimeUnitRange.ISODOW, -1), is(3L)); // wed
    assertThat(unixDateExtract(TimeUnitRange.ISODOW, 0), is(4L)); // thu
    assertThat(unixDateExtract(TimeUnitRange.ISODOW, 1), is(5L)); // fri
    assertThat(unixDateExtract(TimeUnitRange.ISODOW, 2), is(6L)); // sat
    assertThat(unixDateExtract(TimeUnitRange.ISODOW, 3), is(7L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.ISODOW, 365), is(5L));
    assertThat(unixDateExtract(TimeUnitRange.ISODOW, 366), is(6L));

    assertThat(unixDateExtract(TimeUnitRange.DOY, -1), is(365L));
    assertThat(unixDateExtract(TimeUnitRange.DOY, 0), is(1L));
    assertThat(unixDateExtract(TimeUnitRange.DOY, 1), is(2L));
    assertThat(unixDateExtract(TimeUnitRange.DOY, 2), is(3L));
    assertThat(unixDateExtract(TimeUnitRange.DOY, 3), is(4L));
    assertThat(unixDateExtract(TimeUnitRange.DOY, 364), is(365L));
    assertThat(unixDateExtract(TimeUnitRange.DOY, 365), is(1L));
    assertThat(unixDateExtract(TimeUnitRange.DOY, 366), is(2L));
    assertThat(unixDateExtract(TimeUnitRange.DOY, 365 + 365 + 366 - 1),
        is(366L)); // 1972/12/31
    assertThat(unixDateExtract(TimeUnitRange.DOY, 365 + 365 + 366),
        is(1L)); // 1973/1/1

    // The number of the week of the year that the day is in. By definition
    // (ISO 8601), the first week of a year contains January 4 of that year.
    // (The ISO-8601 week starts on Monday.) In other words, the first Thursday
    // of a year is in week 1 of that year.
    //
    // Because of this, it is possible for early January dates to be part of
    // the 52nd or 53rd week of the previous year. For example, 2005-01-01 is
    // part of the 53rd week of year 2004, and 2006-01-01 is part of the 52nd
    // week of year 2005.
    assertThat(ymdToUnixDate(1970, 1, 1), is(0));
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2003, 1, 1)),
        is(1L)); // wed
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2004, 1, 1)),
        is(1L)); // thu
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2005, 1, 1)),
        is(53L)); // sat
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2005, 1, 2)),
        is(53L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2005, 12, 31)),
        is(52L)); // sat
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2006, 1, 1)),
        is(52L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2006, 1, 2)),
        is(1L)); // mon
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2006, 12, 31)),
        is(52L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2007, 1, 1)),
        is(1L)); // mon
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2007, 12, 30)),
        is(52L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2007, 12, 31)),
        is(1L)); // mon
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2008, 12, 28)),
        is(52L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2008, 12, 29)),
        is(1L)); // mon
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2008, 12, 30)),
        is(1L)); // tue
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2008, 12, 31)),
        is(1L)); // wen
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2009, 1, 1)),
        is(1L)); // thu
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2009, 12, 31)),
        is(53L)); // thu
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2010, 1, 1)),
        is(53L)); // fri
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2010, 1, 2)),
        is(53L)); // sat
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2010, 1, 3)),
        is(53L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2010, 1, 4)),
        is(1L)); // mon
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2012, 12, 30)),
        is(52L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2012, 12, 31)),
        is(1L)); // mon
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2014, 12, 30)),
        is(1L)); // tue
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(2014, 12, 31)),
        is(1L)); // wen
    assertThat(unixDateExtract(TimeUnitRange.WEEK, ymdToUnixDate(1970, 1, 1)),
        is(1L)); // thu

    // Based on the rule: The number of the ISO 8601 week-numbering week of the year.
    // By definition, ISO weeks start on Mondays and the first week of a year contains
    // January 4 of that year. In other words, the first Thursday of a year is in
    // week 1 of that year.
    // For that reason 1969-12-31, 1969-12-30 and 1969-12-29 are in the 1-st ISO week of 1970
    assertThat(unixDateExtract(TimeUnitRange.WEEK, -4), is(52L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.WEEK, -3), is(1L)); // mon
    assertThat(unixDateExtract(TimeUnitRange.WEEK, -2), is(1L)); // tue
    assertThat(unixDateExtract(TimeUnitRange.WEEK, -1), is(1L)); // wed
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 0), is(1L)); // thu
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 1), is(1L)); // fri
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 2), is(1L)); // sat
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 3), is(1L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 4), is(2L)); // mon
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 7), is(2L)); // thu
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 10), is(2L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 11), is(3L)); // mon
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 359), is(52L)); // sat
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 360), is(52L)); // sun
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 361), is(53L)); // mon
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 364), is(53L)); // thu
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 365), is(53L)); // fri
    assertThat(unixDateExtract(TimeUnitRange.WEEK, 368), is(1L)); // mon

    assertThat(unixDateExtract(TimeUnitRange.QUARTER, -1), is(4L));
    assertThat(unixDateExtract(TimeUnitRange.QUARTER, 0), is(1L));
    assertThat(unixDateExtract(TimeUnitRange.QUARTER, 365), is(1L));
    assertThat(unixDateExtract(TimeUnitRange.QUARTER, 366), is(1L));

    thereAndBack(1900, 1, 1);
    thereAndBack(1900, 2, 28); // no leap day
    thereAndBack(1900, 3, 1);
    thereAndBack(1901, 1, 1);
    thereAndBack(1901, 2, 28); // no leap day
    thereAndBack(1901, 3, 1);
    thereAndBack(2000, 1, 1);
    thereAndBack(2000, 2, 28);
    thereAndBack(2000, 2, 29); // leap day
    thereAndBack(2000, 3, 1);
    thereAndBack(1964, 1, 1);
    thereAndBack(1964, 2, 28);
    thereAndBack(1964, 2, 29); // leap day
    thereAndBack(1964, 3, 1);
    thereAndBack(1864, 1, 1);
    thereAndBack(1864, 2, 28);
    thereAndBack(1864, 2, 29); // leap day
    thereAndBack(1864, 3, 1);
    thereAndBack(1900, 1, 1);
    thereAndBack(1900, 2, 28);
    thereAndBack(1900, 3, 1);
    thereAndBack(2004, 2, 28);
    thereAndBack(2004, 2, 29); // leap day
    thereAndBack(2004, 3, 1);
    thereAndBack(2005, 2, 28); // no leap day
    thereAndBack(2005, 3, 1);
    thereAndBack(1601, 1, 1);
    // Doesn't work much earlier than 1600 because of leap year differences.
    // Before 1600, does the user expect Gregorian calendar?
    if (false) {
      thereAndBack(1581, 1, 1);
      thereAndBack(1, 1, 1);
    }

    // Per PostgreSQL: The first century starts at 0001-01-01 00:00:00 AD,
    // although they did not know it at the time. This definition applies to
    // all Gregorian calendar countries. There is no century number 0, you go
    // from -1 century to 1 century. If you disagree with this, please write
    // your complaint to: Pope, Cathedral Saint-Peter of Roma, Vatican.

    // The 21st century started on 2001/01/01
    assertThat(
        unixDateExtract(TimeUnitRange.CENTURY, ymdToUnixDate(2001, 1, 1)),
        is(21L));
    assertThat(
        unixDateExtract(TimeUnitRange.CENTURY, ymdToUnixDate(2000, 12, 31)),
        is(20L));
    assertThat(
        unixDateExtract(TimeUnitRange.CENTURY, ymdToUnixDate(1852, 6, 7)),
        is(19L));
    assertThat(
        unixDateExtract(TimeUnitRange.CENTURY, ymdToUnixDate(1, 2, 1)),
        is(1L));
    assertThat(
        unixDateExtract(TimeUnitRange.CENTURY, ymdToUnixDate(1, 1, 1)),
        is(1L));
    assertThat(
        unixDateExtract(TimeUnitRange.CENTURY, ymdToUnixDate(-2, 1, 1)),
        is(-1L));

    //The 201st decade started on 2010/01/01. A little bit different but based on
    //https://www.postgresql.org/docs/9.1/static/functions-datetime.html#FUNCTIONS-DATETIME-EXTRACT
    assertThat(
        unixDateExtract(TimeUnitRange.DECADE, ymdToUnixDate(2010, 1, 1)),
        is(201L));
    assertThat(
        unixDateExtract(TimeUnitRange.DECADE, ymdToUnixDate(2000, 12, 31)),
        is(200L));
    assertThat(
        unixDateExtract(TimeUnitRange.DECADE, ymdToUnixDate(1852, 6, 7)),
        is(185L));
    assertThat(
        unixDateExtract(TimeUnitRange.DECADE, ymdToUnixDate(1, 2, 1)),
        is(0L));
    // TODO: For a small time range around year 1, due to the Gregorian shift,
    // we end up in the wrong decade. Should be 1.
    assertThat(
        unixDateExtract(TimeUnitRange.DECADE, ymdToUnixDate(1, 1, 1)),
        is(0L));
    assertThat(
        unixDateExtract(TimeUnitRange.DECADE, ymdToUnixDate(-2, 1, 1)),
        is(0L));
    assertThat(
        unixDateExtract(TimeUnitRange.DECADE, ymdToUnixDate(-20, 1, 1)),
        is(-2L));

    // The 3rd millennium started on 2001/01/01
    assertThat(
        unixDateExtract(TimeUnitRange.MILLENNIUM, ymdToUnixDate(2001, 1, 1)),
        is(3L));
    assertThat(
        unixDateExtract(TimeUnitRange.MILLENNIUM, ymdToUnixDate(2000, 12, 31)),
        is(2L));
    assertThat(
        unixDateExtract(TimeUnitRange.MILLENNIUM, ymdToUnixDate(1852, 6, 7)),
        is(2L));
    assertThat(
        unixDateExtract(TimeUnitRange.MILLENNIUM, ymdToUnixDate(1, 1, 1)),
        is(1L));
    assertThat(
        unixDateExtract(TimeUnitRange.MILLENNIUM, ymdToUnixDate(1, 2, 1)),
        is(1L));
    assertThat(
        unixDateExtract(TimeUnitRange.MILLENNIUM, ymdToUnixDate(-2, 1, 1)),
        is(-1L));

    // The ISO 8601 week-numbering year that the date falls in (not applicable
    // to intervals). Each ISO 8601 week-numbering year begins with the Monday
    // of the week containing the 4th of January, so in early January or late
    // December the ISO year may be different from the Gregorian year. See the
    // week field for more information.
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2003, 1, 1)),
        is(2003L)); // wed
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2004, 1, 1)),
        is(2004L)); // thu
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2005, 1, 1)),
        is(2004L)); // sat
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2005, 1, 2)),
        is(2004L)); // sun
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2005, 1, 3)),
        is(2005L)); // mon
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2005, 12, 31)),
        is(2005L)); // sat
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2006, 1, 1)),
        is(2005L)); // sun
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2006, 1, 2)),
        is(2006L)); // mon
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2006, 12, 31)),
        is(2006L)); // sun
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2007, 1, 1)),
        is(2007L)); // mon
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2007, 12, 30)),
        is(2007L)); // sun
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2007, 12, 31)),
        is(2008L)); // mon
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2008, 12, 28)),
        is(2008L)); // sun
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2008, 12, 29)),
        is(2009L)); // mon
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2008, 12, 30)),
        is(2009L)); // tue
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2008, 12, 31)),
        is(2009L)); // wen
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2009, 1, 1)),
        is(2009L)); // thu
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2009, 12, 31)),
        is(2009L)); // thu
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2010, 1, 1)),
        is(2009L)); // fri
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2010, 1, 2)),
        is(2009L)); // sat
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2010, 1, 3)),
        is(2009L)); // sun
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2010, 1, 4)),
        is(2010L)); // mon
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2012, 12, 29)),
        is(2012L)); // sat
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2012, 12, 30)),
        is(2012L)); // sun
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2012, 12, 31)),
        is(2013L)); // mon
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2014, 12, 30)),
        is(2015L)); // tue
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(2014, 12, 31)),
        is(2015L)); // wen
    assertThat(
        unixDateExtract(TimeUnitRange.ISOYEAR, ymdToUnixDate(1970, 1, 1)),
        is(1970L)); // thu

    // For date and timestamp values, the number of seconds since 1970-01-01 00:00:00 UTC
    // (can be negative); for interval values, the total number of seconds in the interval
    assertThat(
        unixDateExtract(TimeUnitRange.EPOCH, ymdToUnixDate(2001, 1, 1)),
        is(978_307_200L));
    assertThat(
        unixDateExtract(TimeUnitRange.EPOCH, ymdToUnixDate(1969, 12, 31)),
        is(-86_400L));
    assertThat(
        unixDateExtract(TimeUnitRange.EPOCH, ymdToUnixDate(1970, 1, 1)),
        is(0L));
    assertThat(
        unixDateExtract(TimeUnitRange.EPOCH, ymdToUnixDate(1, 1, 1)),
        is(-62_135_596_800L));
    assertThat(
        unixDateExtract(TimeUnitRange.EPOCH, ymdToUnixDate(1, 2, 1)),
        is(-62_132_918_400L));
    assertThat(
        unixDateExtract(TimeUnitRange.EPOCH, ymdToUnixDate(-2, 1, 1)),
        is(-62_230_291_200L));
  }

  @Test public void testUnixDate() {
    int days = DateTimeUtils.dateStringToUnixDate("1500-04-30");
    assertThat(DateTimeUtils.unixDateToString(days), is("1500-04-30"));
    assertThat(
        DateTimeUtils.unixDateToString(
            DateTimeUtils.dateStringToUnixDate(
                DateTimeUtils.unixDateToString(
                    DateTimeUtils.dateStringToUnixDate(
                        DateTimeUtils.unixDateToString(
                            DateTimeUtils.dateStringToUnixDate("1500-04-30")))))),
        is("1500-04-30"));

    final int d1900 = -(70 * 365 + 70 / 4);
    final int century = 100 * 365 + 100 / 4;
    checkDateString("1900-01-01", d1900);
    // +1 because 1800 is not a leap year
    final int d1800 = d1900 - century + 1;
    checkDateString("1800-01-01", d1800);
    final int d1700 = d1800 - century + 1;
    checkDateString("1700-01-01", d1700);
    final int d1600 = d1700 - century;
    checkDateString("1600-01-01", d1600);
    final int d1500 = d1600 - century + 1;
    checkDateString("1500-01-01", d1500);
    final int d1400 = d1500 - century + 1;
    checkDateString("1400-01-01", d1400);
    final int d1300 = d1400 - century + 1;
    checkDateString("1300-01-01", d1300);
    final int d1200 = d1300 - century;
    checkDateString("1200-01-01", d1200);
    final int d1100 = d1200 - century + 1;
    checkDateString("1100-01-01", d1100);
    final int d1000 = d1100 - century + 1;
    checkDateString("1000-01-01", d1000);
    final int d900 = d1000 - century + 1;
    checkDateString("0900-01-01", d900);
    final int d800 = d900 - century;
    checkDateString("0800-01-01", d800);
    final int d700 = d800 - century + 1;
    checkDateString("0700-01-01", d700);
    final int d600 = d700 - century + 1;
    checkDateString("0600-01-01", d600);
    final int d500 = d600 - century + 1;
    checkDateString("0500-01-01", d500);
    final int d400 = d500 - century;
    checkDateString("0400-01-01", d400);
    final int d300 = d400 - century + 1;
    checkDateString("0300-01-01", d300);
    final int d200 = d300 - century + 1;
    checkDateString("0200-01-01", d200);
    final int d100 = d200 - century + 1;
    checkDateString("0100-01-01", d100);
    final int d000 = d100 - century;
    checkDateString("0000-01-01", d000);
  }

  @Test public void testDateConversion() {
    for (int i = 0; i < 4000; ++i) {
      for (int j = 1; j <= 12; ++j) {
        String date = String.format(Locale.ENGLISH, "%04d-%02d-28", i, j);
        assertThat(unixDateToString(ymdToUnixDate(i, j, 28)), is(date));
      }
    }
  }

  private void thereAndBack(int year, int month, int day) {
    final int unixDate = ymdToUnixDate(year, month, day);
    assertThat(unixDateExtract(TimeUnitRange.YEAR, unixDate),
        is((long) year));
    assertThat(unixDateExtract(TimeUnitRange.MONTH, unixDate),
        is((long) month));
    assertThat(unixDateExtract(TimeUnitRange.DAY, unixDate),
        is((long) day));
    final long isoYear = unixDateExtract(TimeUnitRange.ISOYEAR, unixDate);
    assertTrue(isoYear >= year - 1 && isoYear <= year + 1);
    final long w = unixDateExtract(TimeUnitRange.WEEK, unixDate);
    assertTrue(w >= 1 && w <= 53);
    final long dow = unixDateExtract(TimeUnitRange.DOW, unixDate);
    assertTrue(dow >= 1 && dow <= 7);
    final long iso_dow = unixDateExtract(TimeUnitRange.ISODOW, unixDate);
    assertTrue(iso_dow >= 1 && iso_dow <= 7);
    final long doy = unixDateExtract(TimeUnitRange.DOY, unixDate);
    assertTrue(doy >= 1 && doy <= 366);
    final long q = unixDateExtract(TimeUnitRange.QUARTER, unixDate);
    assertTrue(q >= 1 && q <= 4);
    final long d = unixDateExtract(TimeUnitRange.DECADE, unixDate);
    assertTrue(d == year / 10);
    final long c = unixDateExtract(TimeUnitRange.CENTURY, unixDate);
    assertTrue(c == (year > 0 ? (year + 99) / 100 : (year - 99) / 100));
    final long m = unixDateExtract(TimeUnitRange.MILLENNIUM, unixDate);
    assertTrue(m == (year > 0 ? (year + 999) / 1000 : (year - 999) / 1000));
  }

  @Test public void testAddMonths() {
    checkAddMonths(2016, 1, 1, 2016, 2, 1, 1);
    checkAddMonths(2016, 1, 1, 2017, 1, 1, 12);
    checkAddMonths(2016, 1, 1, 2017, 2, 1, 13);
    checkAddMonths(2016, 1, 1, 2015, 1, 1, -12);
    checkAddMonths(2016, 1, 1, 2018, 10, 1, 33);
    checkAddMonths(2016, 1, 31, 2016, 5, 1, 3); // roll up
    checkAddMonths(2016, 4, 30, 2016, 7, 30, 3); // roll up
    checkAddMonths(2016, 1, 31, 2016, 3, 1, 1);
    checkAddMonths(2016, 3, 31, 2016, 3, 1, -1);
    checkAddMonths(2016, 3, 31, 2116, 3, 31, 1200);
    checkAddMonths(2016, 2, 28, 2116, 2, 28, 1200);
    checkAddMonths(2019, 9, 1, 2020, 3, 1, 6);
    checkAddMonths(2019, 9, 1, 2016, 8, 1, -37);
  }

  private void checkAddMonths(int y0, int m0, int d0, int y1, int m1, int d1,
      int months) {
    final int date0 = ymdToUnixDate(y0, m0, d0);
    final long date = addMonths(date0, months);
    final int date1 = ymdToUnixDate(y1, m1, d1);
    assertThat((int) date, is(date1));

    assertThat(subtractMonths(date1, date0),
        anyOf(is(months), is(months + 1)));
    assertThat(subtractMonths(date1 + 1, date0),
        anyOf(is(months), is(months + 1)));
    assertThat(subtractMonths(date1, date0 + 1),
        anyOf(is(months), is(months - 1)));
    assertThat(subtractMonths(d2ts(date1, 1), d2ts(date0, 0)),
        anyOf(is(months), is(months + 1)));
    assertThat(subtractMonths(d2ts(date1, 0), d2ts(date0, 1)),
        anyOf(is(months - 1), is(months), is(months + 1)));
  }

  /** Converts a date (days since epoch) and milliseconds (since midnight)
   * into a timestamp (milliseconds since epoch). */
  private long d2ts(int date, int millis) {
    return date * DateTimeUtils.MILLIS_PER_DAY + millis;
  }

  @Test public void testUnixTimestamp() {
    assertThat(unixTimestamp(1970, 1, 1, 0, 0, 0), is(0L));
    final long day = 86400000L;
    assertThat(unixTimestamp(1970, 1, 2, 0, 0, 0), is(day));
    assertThat(unixTimestamp(1970, 1, 1, 23, 59, 59), is(86399000L));

    // 1900 is not a leap year
    final long y1900 = -2203977600000L;
    assertThat(unixTimestamp(1900, 2, 28, 0, 0, 0), is(y1900));
    assertThat(unixTimestamp(1900, 3, 1, 0, 0, 0), is(y1900 + day));

    // 2000 is a leap year
    final long y2k = 951696000000L;
    assertThat(unixTimestamp(2000, 2, 28, 0, 0, 0), is(y2k));
    assertThat(unixTimestamp(2000, 2, 29, 0, 0, 0), is(y2k + day));
    assertThat(unixTimestamp(2000, 3, 1, 0, 0, 0), is(y2k + day + day));

    // 2016 is a leap year
    final long y2016 = 1456617600000L;
    assertThat(unixTimestamp(2016, 2, 28, 0, 0, 0), is(y2016));
    assertThat(unixTimestamp(2016, 2, 29, 0, 0, 0), is(y2016 + day));
    assertThat(unixTimestamp(2016, 3, 1, 0, 0, 0), is(y2016 + day + day));
  }

  @Test public void testParse() {
    final SimpleDateFormat formatD =
        new SimpleDateFormat(DateTimeUtils.DATE_FORMAT_STRING, Locale.ROOT);
    final Calendar c =
        DateTimeUtils.parseDateFormat("1234-04-12", formatD,
            DateTimeUtils.UTC_ZONE);
    assertThat(c, notNullValue());
    assertThat(c.get(Calendar.YEAR), is(1234));
    assertThat(c.get(Calendar.MONTH), is(Calendar.APRIL));
    assertThat(c.get(Calendar.DAY_OF_MONTH), is(12));

    final SimpleDateFormat formatTs =
        new SimpleDateFormat(DateTimeUtils.TIMESTAMP_FORMAT_STRING,
            Locale.ROOT);
    final DateTimeUtils.PrecisionTime pt =
        DateTimeUtils.parsePrecisionDateTimeLiteral(
            "1234-04-12 01:23:45.06789", formatTs, DateTimeUtils.UTC_ZONE, -1);
    assertThat(pt, notNullValue());
    assertThat(pt.getCalendar().get(Calendar.YEAR), is(1234));
    assertThat(pt.getCalendar().get(Calendar.MONTH), is(Calendar.APRIL));
    assertThat(pt.getCalendar().get(Calendar.DAY_OF_MONTH), is(12));
    assertThat(pt.getCalendar().get(Calendar.HOUR_OF_DAY), is(1));
    assertThat(pt.getCalendar().get(Calendar.MINUTE), is(23));
    assertThat(pt.getCalendar().get(Calendar.SECOND), is(45));
    assertThat(pt.getCalendar().get(Calendar.MILLISECOND), is(67));
    assertThat(pt.getFraction(), is("06789"));
    assertThat(pt.getPrecision(), is(5));

    // as above, but limit to 2 fractional digits
    final DateTimeUtils.PrecisionTime pt2 =
        DateTimeUtils.parsePrecisionDateTimeLiteral(
            "1234-04-12 01:23:45.06789", formatTs, DateTimeUtils.UTC_ZONE, 2);
    assertThat(pt2, notNullValue());
    assertThat(pt2.getCalendar().get(Calendar.MILLISECOND), is(60));
    assertThat(pt2.getFraction(), is("06"));
  }

  @Test public void testUnixDateFloorCeil() {
    final long y1001 = ymdToUnixDate(1001, 1, 1);
    final long y1801 = -(169 * 365 + 169 / 4 - 1);
    final long y1890 = -(80 * 365 + 80 / 4 - 1);
    final long y1900 = -(70 * 365 + 70 / 4);
    final long y1910 = -(60 * 365 + 60 / 4);
    final long y1907 = -(63 * 365 + 63 / 4);
    final long y2001 = 31 * 365 + 31 / 4 + 1;
    final long y1900_0102 = y1900 + 1;
    final long y1899 = y1900 - 365;
    final long y1901 = y1900 + 365;
    final long y1900_0506 = y1900 - 1 + 31 + 28 + 31 + 30 + 6; // sunday
    final long y1900_0512 = y1900 - 1 + 31 + 28 + 31 + 30 + 12; // saturday
    final long y1900_0513 = y1900 - 1 + 31 + 28 + 31 + 30 + 13; // sunday
    final long y1900_0514 = y1900 - 1 + 31 + 28 + 31 + 30 + 14; // monday
    final long y1900_0520 = y1900 - 1 + 31 + 28 + 31 + 30 + 20; // sunday
    final long y1900_0401 = y1900 - 1 + 31 + 28 + 31 + 1;
    final long y1900_0501 = y1900 - 1 + 31 + 28 + 31 + 30 + 1;
    final long y1900_0601 = y1900 - 1 + 31 + 28 + 31 + 30 + 31 + 1;
    final long y1900_0701 = y1900 - 1 + 31 + 28 + 31 + 30 + 31 + 30 + 1;
    final long y1900_1001 = y1900 - 1 + 31 + 28 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 1;
    final long y1900_1002 = y1900 - 1 + 31 + 28 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 2;
    checkDateString("1801-01-01", (int) y1801);
    checkDateString("1900-01-01", (int) y1900);
    checkDateString("1900-01-02", (int) y1900_0102);
    checkDateString("1899-01-01", (int) y1899);
    checkDateString("1901-01-01", (int) y1901);
    checkDateString("2001-01-01", (int) y2001);
    assertThat(unixDateFloor(TimeUnitRange.MILLENNIUM, y1907), is(y1001));
    assertThat(unixDateCeil(TimeUnitRange.MILLENNIUM, y1907), is(y2001));
    assertThat(unixDateFloor(TimeUnitRange.CENTURY, y1899), is(y1801));
    assertThat(unixDateCeil(TimeUnitRange.CENTURY, y1899), is(y1901));
    assertThat(unixDateFloor(TimeUnitRange.DECADE, y1899), is(y1890));
    assertThat(unixDateFloor(TimeUnitRange.DECADE, y1900_0701), is(y1900));
    assertThat(unixDateFloor(TimeUnitRange.DECADE, y1907), is(y1900));
    assertThat(unixDateCeil(TimeUnitRange.DECADE, y1899), is(y1900));
    assertThat(unixDateCeil(TimeUnitRange.DECADE, y1900_0701), is(y1910));
    assertThat(unixDateCeil(TimeUnitRange.DECADE, y1907), is(y1910));
    assertThat(unixDateFloor(TimeUnitRange.YEAR, y1900_0102), is(y1900));
    assertThat(unixDateCeil(TimeUnitRange.YEAR, y1900_0102), is(y1901));
    assertThat(unixDateFloor(TimeUnitRange.QUARTER, y1900_0514), is(y1900_0401));
    assertThat(unixDateCeil(TimeUnitRange.QUARTER, y1900_0514), is(y1900_0701));
    assertThat(unixDateFloor(TimeUnitRange.QUARTER, y1900_1001), is(y1900_1001));
    assertThat(unixDateCeil(TimeUnitRange.QUARTER, y1900_1001), is(y1900_1001));
    assertThat(unixDateFloor(TimeUnitRange.QUARTER, y1900_1002), is(y1900_1001));
    assertThat(unixDateCeil(TimeUnitRange.QUARTER, y1900_1002), is(y1901));
    assertThat(unixDateFloor(TimeUnitRange.MONTH, y1900_0514), is(y1900_0501));
    assertThat(unixDateCeil(TimeUnitRange.MONTH, y1900_0514), is(y1900_0601));
    assertThat(unixDateFloor(TimeUnitRange.WEEK, y1900_0514), is(y1900_0513));
    assertThat(unixDateCeil(TimeUnitRange.WEEK, y1900_0514), is(y1900_0520));
    assertThat(unixDateFloor(TimeUnitRange.WEEK, y1900_0514), is(y1900_0513));
    assertThat(unixDateCeil(TimeUnitRange.WEEK, y1900_0514), is(y1900_0520));
    assertThat(unixDateFloor(TimeUnitRange.WEEK, y1900_0513), is(y1900_0513));
    assertThat(unixDateCeil(TimeUnitRange.WEEK, y1900_0513), is(y1900_0513));
    assertThat(unixDateFloor(TimeUnitRange.WEEK, y1900_0512), is(y1900_0506));
    assertThat(unixDateCeil(TimeUnitRange.WEEK, y1900_0512), is(y1900_0513));
    assertThat(unixDateFloor(TimeUnitRange.DAY, y1900_0514), is(y1900_0514));
    assertThat(unixDateCeil(TimeUnitRange.DAY, y1900_0514), is(y1900_0514));
  }
}

// End DateTimeUtilsTest.java
