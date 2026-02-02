package dev.mini.project.blog.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

public class SortUtil {

    private static final Logger logger = LoggerFactory.getLogger(SortUtil.class);

    private SortUtil() {};


    /**
     * parse sort direction
     *
     * @param sortDirection sortDirection
     * @return {@link Sort}
     * @see Sort
     */
    public static Sort parseSortDirection(String sortDirection, String sortByField) {
        logger.info("SortUtil#parseSortDirection({}, {})", sortDirection, sortByField);

        return switch(sortDirection.toLowerCase()){
            case "asc", "ascending" -> Sort.by(sortByField).ascending();
            case "desc", "descending" -> Sort.by(sortByField).descending();
            default -> {
                logger.error("Invalid sortDirection {}", sortDirection);
                throw new IllegalArgumentException("Invalid sortDirection: " + sortDirection);
            }
        };
    }
}
