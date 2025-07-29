import { Grid, Typography } from "@mui/material";
import TableViewBanner from "../components/TableViewBanner";
import { type Table } from "../interfaces/bookings";
import { useState } from "react";
import { type Location } from "../interfaces/locations";
import TableCard from "../components/TableCard";
import { useTranslation } from "react-i18next";
import TableCardSkeleton from "../components/TableCardSkeleton";
function TablePage() {
  const [tables, setTables] = useState<Table[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedLocation, setSelectedLocation] = useState<Location>();
  const { t } = useTranslation();

  return (
    <Grid container spacing={2}>
      <Grid size={{ xs: 12 }}>
        <TableViewBanner
          setTables={setTables}
          setSelectedLocation={setSelectedLocation}
          setIsLoading={setIsLoading}
        />
      </Grid>
      <Grid size={{ xs: 12 }} p={2}>
        <Typography variant="h6">
          {tables.length} {t("tables_available")}
        </Typography>
        <Grid
          container
          spacing={6}
          sx={{ display: "flex", flexWrap: "wrap", mt: 2 }}
        >
          {isLoading && <TableCardSkeleton />}
          {!isLoading && (
            <>
              {selectedLocation && (
                <>
                  {tables.map((table) => (
                    <Grid
                      size={{ xs: 12, md: 6, lg: 6 }}
                      key={table.tableNumber}
                    >
                      <TableCard
                        table={table}
                        selectedLocation={selectedLocation}
                      />
                    </Grid>
                  ))}
                </>
              )}
            </>
          )}
        </Grid>
      </Grid>
    </Grid>
  );
}

export default TablePage;
