package de.komoot.photon.query;

import com.google.common.collect.ImmutableSet;
import com.vividsolutions.jts.geom.Point;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Map;
import java.util.Set;

/**
 * @author svantulden
 */
public class ReverseQueryBuilder implements TagFilterQueryBuilder {
    private Integer limit;

    private Double radius;

    private Point location;

    private String queryStringMust;

    private String queryStringMustNot;

    private ReverseQueryBuilder(Point location, Double radius, String queryStringMust, String queryStringMustNot) {
        this.location = location;
        this.radius = radius;
        this.queryStringMust = queryStringMust;
        this.queryStringMustNot = queryStringMustNot;
    }

    public static TagFilterQueryBuilder builder(Point location, Double radius, String queryStringMust, String queryStringMustNot) {
        return new ReverseQueryBuilder(location, radius, queryStringMust, queryStringMustNot);
    }

    @Override
    public TagFilterQueryBuilder withLimit(Integer limit) {
        this.limit = limit == null || limit < 0 ? 0 : limit;
        this.limit = this.limit > 5000 ? 5000 : this.limit;

        return this;
    }

    @Override
    public TagFilterQueryBuilder withLocationBias(Point point, double scale) {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }

    @Override
    public TagFilterQueryBuilder withTags(Map<String, Set<String>> tags) {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }

    @Override
    public TagFilterQueryBuilder withKeys(Set<String> keys) {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }

    @Override
    public TagFilterQueryBuilder withValues(Set<String> values) {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }

    @Override
    public TagFilterQueryBuilder withTagsNotValues(Map<String, Set<String>> tags) {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }

    @Override
    public TagFilterQueryBuilder withoutTags(Map<String, Set<String>> tagsToExclude) {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }

    @Override
    public TagFilterQueryBuilder withoutKeys(Set<String> keysToExclude) {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }

    @Override
    public TagFilterQueryBuilder withoutValues(Set<String> valuesToExclude) {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }

    @Override
    public TagFilterQueryBuilder withKeys(String... keys) {
        return this.withKeys(ImmutableSet.<String>builder().add(keys).build());
    }

    @Override
    public TagFilterQueryBuilder withValues(String... values) {
        return this.withValues(ImmutableSet.<String>builder().add(values).build());
    }

    @Override
    public TagFilterQueryBuilder withoutKeys(String... keysToExclude) {
        return this.withoutKeys(ImmutableSet.<String>builder().add(keysToExclude).build());
    }

    @Override
    public TagFilterQueryBuilder withoutValues(String... valuesToExclude) {
        return this.withoutValues(ImmutableSet.<String>builder().add(valuesToExclude).build());
    }

    @Override
    public TagFilterQueryBuilder withStrictMatch() {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }

    @Override
    public TagFilterQueryBuilder withLenientMatch() {
        throw new RuntimeException(new NoSuchMethodException("this method is not implemented (NOOP)"));
    }

    @Override
    public QueryBuilder buildQuery() {
        QueryBuilder fb = QueryBuilders.geoDistanceQuery("coordinate").point(location.getY(), location.getX())
                .distance(radius, DistanceUnit.KILOMETERS);

        BoolQueryBuilder finalQuery;
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (queryStringMust != null || queryStringMustNot != null)
            boolQuery = boolQuery.must(QueryBuilders.matchAllQuery());

        if (queryStringMust != null && queryStringMust.trim().length() > 0)
            boolQuery = boolQuery.must(QueryBuilders.queryStringQuery(queryStringMust));

        if (queryStringMustNot != null && queryStringMustNot.trim().length() > 0)
            boolQuery = boolQuery.mustNot(QueryBuilders.queryStringQuery(queryStringMustNot));

        finalQuery = boolQuery.filter(fb);

        return finalQuery;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }

    private Boolean checkTags(Set<String> keys) {
        return !(keys == null || keys.isEmpty());
    }

    private Boolean checkTags(Map<String, Set<String>> tags) {
        return !(tags == null || tags.isEmpty());
    }

    private enum State {
        PLAIN, FILTERED, QUERY_ALREADY_BUILT, FINISHED,
    }
}
