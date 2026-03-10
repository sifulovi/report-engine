package com.jasperframework.exporter;

/**
 * Defines a range of pages to export from a filled report.
 * <p>
 * Page indices are zero-based. Both {@code startPage} and {@code endPage}
 * are inclusive.
 *
 * <pre>{@code
 * // Export only the first 5 pages (indices 0-4)
 * PageRange range = PageRange.of(0, 4);
 *
 * // Export a single page
 * PageRange range = PageRange.single(2);
 *
 * // Export all pages
 * PageRange range = PageRange.all();
 * }</pre>
 */
public final class PageRange {

    private static final PageRange ALL = new PageRange(-1, -1);

    private final int startPage;
    private final int endPage;

    private PageRange(int startPage, int endPage) {
        this.startPage = startPage;
        this.endPage = endPage;
    }

    /**
     * Creates a page range from {@code startPage} to {@code endPage} (inclusive, zero-based).
     */
    public static PageRange of(int startPage, int endPage) {
        if (startPage < 0) {
            throw new IllegalArgumentException("startPage must be >= 0");
        }
        if (endPage < startPage) {
            throw new IllegalArgumentException("endPage must be >= startPage");
        }
        return new PageRange(startPage, endPage);
    }

    /**
     * Creates a page range for a single page (zero-based index).
     */
    public static PageRange single(int page) {
        return of(page, page);
    }

    /**
     * Returns a sentinel that represents all pages.
     */
    public static PageRange all() {
        return ALL;
    }

    public int getStartPage() { return startPage; }
    public int getEndPage() { return endPage; }

    /**
     * Returns {@code true} if this range represents all pages.
     */
    public boolean isAll() {
        return this == ALL || (startPage == -1 && endPage == -1);
    }

    @Override
    public String toString() {
        return isAll() ? "PageRange[ALL]"
                : "PageRange[" + startPage + "-" + endPage + "]";
    }
}
