package org.tamtamcatworks.auction.client;

/**
 * Immutable carrier of all search/filter intent passed between controllers.
 *
 * <p>Factory methods provide convenient entry-points for the most common navigation scenarios
 * (category shelf "See all →", status pill click, etc.).
 */
public record SearchParams(
    /** Free-text query typed by the user. Empty string means "no query". */
    String query,
    /**
     * Category string matching the API values: "All categories", "Art", "Electronics", "Vehicle",
     * "Other".
     */
    String category,
    /** Status filter: "ALL", "ACTIVE", "PENDING", "CLOSED". */
    String status,
    /**
     * Sort-order label matching the values in the sortCombo: "Newest First", "Ending Soon", "Most
     * Bids", "Price: Low to High", "Price: High to Low", "Oldest First".
     */
    String sortOrder,
    /** 0-based page index. */
    int page) {

  /** No filters, no query — shows everything, default sort. */
  public static SearchParams empty() {
    return new SearchParams("", "All categories", "ALL", "Newest First", 0);
  }

  /** Pre-filtered to a specific category (human-readable label). */
  public static SearchParams forCategory(String category) {
    return new SearchParams("", category, "ALL", "Newest First", 0);
  }

  /** Pre-filtered to a specific status string (e.g. "ACTIVE"). */
  public static SearchParams forStatus(String status) {
    return new SearchParams("", "All categories", status, "Newest First", 0);
  }

  /** Pre-filled with a search query, no other filters. */
  public static SearchParams forQuery(String query) {
    return new SearchParams(query, "All categories", "ALL", "Newest First", 0);
  }

  /** Pre-filled with query + category. */
  public static SearchParams forQueryAndCategory(String query, String category) {
    return new SearchParams(query, category, "ALL", "Newest First", 0);
  }
}
