package com.jasperframework.exporter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageRangeTest {

    @Test
    void ofCreatesRange() {
        PageRange range = PageRange.of(2, 5);
        assertThat(range.getStartPage()).isEqualTo(2);
        assertThat(range.getEndPage()).isEqualTo(5);
        assertThat(range.isAll()).isFalse();
    }

    @Test
    void singleCreatesOnePageRange() {
        PageRange range = PageRange.single(3);
        assertThat(range.getStartPage()).isEqualTo(3);
        assertThat(range.getEndPage()).isEqualTo(3);
        assertThat(range.isAll()).isFalse();
    }

    @Test
    void allIsMarkedAsAll() {
        PageRange range = PageRange.all();
        assertThat(range.isAll()).isTrue();
    }

    @Test
    void negativeStartPageThrows() {
        assertThatThrownBy(() -> PageRange.of(-1, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void endBeforeStartThrows() {
        assertThatThrownBy(() -> PageRange.of(5, 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toStringShowsRange() {
        assertThat(PageRange.of(0, 4).toString()).isEqualTo("PageRange[0-4]");
        assertThat(PageRange.all().toString()).isEqualTo("PageRange[ALL]");
    }
}
