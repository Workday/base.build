package build.base.table;

import build.base.foundation.Strings;
import build.base.table.option.CellSeparator;
import build.base.table.option.RowComparator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Table}.
 *
 * @author brian.oliver
 * @since Dec-2018
 */
class TableTests {

    /**
     * Ensure a newly created {@link Table} is output as an empty string.
     */
    @Test
    void shouldOutputEmptyTable() {
        final var table = Table.create();

        assertThat(table.toString())
            .isEmpty();
    }

    /**
     * Ensure a {@link Table} containing a single cell is output as a {@link String} followed by a newline.
     */
    @Test
    void shouldOutputSingleCellTable() {
        final var table = Table.of(Row.of("hello"));

        assertThat(table.toString())
            .isEqualTo("hello\n");
    }

    /**
     * Ensure a {@link Table} containing a single row of two cells is output as a {@link String} using the
     * default {@link CellSeparator} followed by a newline.
     */
    @Test
    void shouldOutputSingleRowSingleColumnTable() {
        final var table = Table.of(Row.of("hello", "world"));

        assertThat(table.toString())
            .isEqualTo("hello : world\n");
    }

    /**
     * Ensure a {@link Table} containing two rows of one cell is output as a {@link String}
     * with a newline following each row.
     */
    @Test
    void shouldOutputMultiRowSingleColumnTable() {
        final var table = Table.of(
            Row.of("hello"),
            Row.of("world"));

        assertThat(table.toString())
            .isEqualTo("hello\nworld\n");
    }

    /**
     * Ensure a {@link Table} containing two rows of two cells is output as a {@link String}
     * using the default {@link CellSeparator} between columns, with the width of columns automatically calculated
     * and a newline following each row.
     */
    @Test
    void shouldOutputMultiRowMultiColumnTable() {
        final var table = Table.of(
            Row.of("hello", "world"),
            Row.of("gudday", "mate"));

        assertThat(table.toString())
            .isEqualTo("hello  : world\ngudday : mate\n");
    }

    /**
     * Ensure a {@link Table} containing multiple rows of multiple cells and a {@link RowComparator}
     * is output as a {@link String} where the rows are automatically sorted (according to the {@link RowComparator}),
     * each cell in each row being separated by the default {@link CellSeparator} and each row being followed by
     * a newline.
     */
    @Test
    void shouldSortAndOutputMultiRowMultiColumnTable() {
        final var table = Table.of(
            Row.of("b", "hello"),
            Row.of("a", "gudday"),
            Row.of("d", "Bonjour"),
            Row.of("c", "hi"));

        table.options().add(RowComparator.orderByColumn(0));

        assertThat(table.toString())
            .isEqualTo("a : gudday\nb : hello\nc : hi\nd : Bonjour\n");
    }

    /**
     * Ensure a {@link Table} containing multiple rows of multiple cells, a header row and a {@link RowComparator}
     * is output as a {@link String} where the rows are automatically sorted (according to the {@link RowComparator}),
     * each cell in each row being separated by the default {@link CellSeparator} and each row being followed by
     * a newline.
     */
    @Test
    void shouldSortAndOutputMultiRowMultiColumnWithHeaderTable() {
        final var table = Table.of(
            Row.header("letter", "message"),
            Row.of("b", "hello"),
            Row.of("a", "gudday"),
            Row.of("d", "Bonjour"), Row.of("c", "hi"));

        table.options().add(RowComparator.orderByColumn(0));

        assertThat(table.toString())
            .isEqualTo("letter : message\na      : gudday\nb      : hello\nc      : hi\nd      : Bonjour\n");
    }

    /**
     * Ensure a {@link Table} containing single and empty {@link Cell}s
     * can be nested in another {@link Table}.
     */
    @Test
    void shouldNestTables() {
        final var emptyTable = Table.create();

        final var singleCellTable = Table.of(Row.of("Zero"));

        final var inner = Table.of(
            Row.of("Inner - First", "Inner - Second"),
            Row.of("Inner - Third", "Inner - Fourth"),
            Row.of(emptyTable.toString(), emptyTable.toString()));

        final var outer = Table.of(
            Row.of("Fifth", inner.toString()),
            Row.of(inner.toString(), "Sixth"),
            Row.of(singleCellTable.toString(), "Eighth"));

        final var expected =
            "Fifth                          : Inner - First : Inner - Second\n"
                + "                               : Inner - Third : Inner - Fourth\n"
                + "                               :               :\n"
                + "Inner - First : Inner - Second : Sixth\n"
                + "Inner - Third : Inner - Fourth : \n"
                + "              :                : \n"
                + "Zero                           : Eighth\n";

        assertThat(outer.toString())
            .isEqualTo(expected);
    }

    /**
     * Ensure {@link Table#getRow} returns null for an out-of-bounds index equal to the row count.
     */
    @Test
    void getRowShouldReturnNullForIndexEqualToSize() {
        final var table = Table.of(Row.of("only"));

        assertThat(table.getRow(1)).isNull();
    }

    /**
     * Ensure irregularly shaped {@link Table}s, with different numbers of cells per row, can be output.
     */
    @Test
    void shouldOutputIrregularTables() {
        final var table = Table.of(
            Row.of(""),
            Row.of("1"),
            Row.of("2", "2", "2"),
            Row.of("3", "3"),
            Row.of(""));

        final var expected = "  :   : \n" + "1 :   : \n" + "2 : 2 : 2\n" + "3 : 3 : \n" + "  :   : \n";

        assertThat(table.toString())
            .isEqualTo(expected);
    }

    /**
     * Ensure a {@link Table} containing an ignored cell width is output as a {@link String}.
     */
    @Test
    void shouldIgnoreLastCellWidth() {

        final var ignore = Cell.of(Strings.repeat("<ignore me>", 20));

        final var table = Table.of(
            Row.of("hello", "world"),
            Row.of(Cell.of("no impact"), ignore),
            Row.of("gudday", "mate"));

        final var string = table.toString();

        assertThat(string)
            .contains("world\n");

        assertThat(string)
            .contains("mate\n");
    }
}
