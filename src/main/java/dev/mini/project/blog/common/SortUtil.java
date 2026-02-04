package dev.mini.project.blog.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

@Slf4j
public class SortUtil {

    private SortUtil() {};

    /**
     * parse sort direction
     *
     * @param sortDirection sortDirection
     * @return {@link Sort}
     * @see Sort
     */
    public static Sort parseSortDirection(String sortDirection, String sortByField) {
        log.info("SortUtil#parseSortDirection({}, {})", sortDirection, sortByField);

        return switch(sortDirection.toLowerCase()){
            case "asc", "ascending" -> Sort.by(sortByField).ascending();
            case "desc", "descending" -> Sort.by(sortByField).descending();
            default -> {
                log.error("Invalid sortDirection {}", sortDirection);
                throw new IllegalArgumentException("Invalid sortDirection: " + sortDirection);
            }
        };
    }
}
