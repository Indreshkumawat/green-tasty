import {
  DataGrid,
  type GridColDef,
  type GridRenderCellParams,
} from "@mui/x-data-grid";
import { Box, Typography, Tooltip } from "@mui/material";

const staffPerformanceColumns: GridColDef[] = [
  {
    field: "locationId",
    headerName: "Location",
    flex: 1,
    align: "center",
    headerAlign: "center",
  },
  {
    field: "waiterName",
    headerName: "Waiter",
    flex: 1,
    align: "center",
    headerAlign: "center",
  },
  {
    field: "startDate",
    headerName: "Report period start",
    flex: 1,
    align: "center",
    headerAlign: "center",
    renderCell: (params) => (
      <Typography color="primary" sx={{ width: "100%", textAlign: "center" }}>
        {params.value}
      </Typography>
    ),
  },
  {
    field: "endDate",
    headerName: "Report period end",
    flex: 1,
    align: "center",
    headerAlign: "center",
    renderCell: (params) => (
      <Typography color="primary" sx={{ width: "100%", textAlign: "center" }}>
        {params.value}
      </Typography>
    ),
  },
  {
    field: "workingHours",
    headerName: "Working Hours",
    flex: 1,
    align: "center",
    headerAlign: "center",
    renderCell: (params) => (
      <Typography sx={{ width: "100%", textAlign: "center" }}>
        {Number(params.value).toFixed(1)}
      </Typography>
    ),
  },
  {
    field: "ordersProcessed",
    headerName: "Orders Processed",
    flex: 1,
    align: "center",
    headerAlign: "center",
    renderCell: (params) => (
      <Typography sx={{ width: "100%", textAlign: "center" }}>
        {Number(params.value).toFixed(0)}
      </Typography>
    ),
  },
  {
    field: "deltaOrdersProcessed",
    headerName: "Orders Change",
    flex: 1.5,
    align: "center",
    headerAlign: "center",
    renderHeader: () => (
      <Tooltip title="Change in orders processed compared to previous period (%)">
        <Typography sx={{ width: "100%", textAlign: "center" }}>
          Orders Change (%)
        </Typography>
      </Tooltip>
    ),
    renderCell: (params: GridRenderCellParams<any>) => (
      <Typography
        sx={{ width: "100%", textAlign: "center" }}
        color={
          params.value > 0
            ? "success.main"
            : params.value < 0
              ? "error.main"
              : "textPrimary"
        }
      >
        {params.value > 0
          ? `+${Number(params.value).toFixed(2)}%`
          : `${Number(params.value).toFixed(2)}%`}
      </Typography>
    ),
  },
  {
    field: "avgServiceFeedback",
    headerName: "Avg Service Rating",
    flex: 1,
    align: "center",
    headerAlign: "center",
    renderCell: (params) => (
      <Typography sx={{ width: "100%", textAlign: "center" }}>
        {Number(params.value).toFixed(2)}
      </Typography>
    ),
  },
  {
    field: "deltaServiceFeedback",
    headerName: "Service Rating Change",
    flex: 1.5,
    align: "center",
    headerAlign: "center",
    renderHeader: () => (
      <Tooltip title="Change in service rating compared to previous period (%)">
        <Typography sx={{ width: "100%", textAlign: "center" }}>
          Service Rating Change (%)
        </Typography>
      </Tooltip>
    ),
    renderCell: (params: GridRenderCellParams<any>) => (
      <Typography
        sx={{ width: "100%", textAlign: "center" }}
        color={
          params.value > 0
            ? "success.main"
            : params.value < 0
              ? "error.main"
              : "textPrimary"
        }
      >
        {params.value > 0
          ? `+${Number(params.value).toFixed(2)}%`
          : `${Number(params.value).toFixed(2)}%`}
      </Typography>
    ),
  },
];

const salesColumns: GridColDef[] = [
  {
    field: "locationId",
    headerName: "Location",
    flex: 1,
    align: "center",
    headerAlign: "center",
  },
  {
    field: "startDate",
    headerName: "Report period start",
    flex: 1,
    align: "center",
    headerAlign: "center",
    renderCell: (params) => (
      <Typography color="primary" sx={{ width: "100%", textAlign: "center" }}>
        {params.value}
      </Typography>
    ),
  },
  {
    field: "endDate",
    headerName: "Report period end",
    flex: 1,
    align: "center",
    headerAlign: "center",
    renderCell: (params) => (
      <Typography color="primary" sx={{ width: "100%", textAlign: "center" }}>
        {params.value}
      </Typography>
    ),
  },
  {
    field: "ordersProcessed",
    headerName: "Orders Processed",
    flex: 1,
    align: "center",
    headerAlign: "center",
    renderCell: (params) => (
      <Typography sx={{ width: "100%", textAlign: "center" }}>
        {Number(params.value).toFixed(0)}
      </Typography>
    ),
  },
  {
    field: "deltaOrdersProcessed",
    headerName: "Orders Change",
    flex: 1.5,
    align: "center",
    headerAlign: "center",
    renderHeader: () => (
      <Tooltip title="Change in orders processed compared to previous period (%)">
        <Typography sx={{ width: "100%", textAlign: "center" }}>
          Orders Change (%)
        </Typography>
      </Tooltip>
    ),
    renderCell: (params: GridRenderCellParams<any>) => (
      <Typography
        sx={{ width: "100%", textAlign: "center" }}
        color={
          params.value > 0
            ? "success.main"
            : params.value < 0
              ? "error.main"
              : "textPrimary"
        }
      >
        {params.value > 0
          ? `+${Number(params.value).toFixed(2)}%`
          : `${Number(params.value).toFixed(2)}%`}
      </Typography>
    ),
  },
  {
    field: "revenue",
    headerName: "Revenue",
    flex: 1,
    align: "center",
    headerAlign: "center",
    renderCell: (params) => (
      <Typography sx={{ width: "100%", textAlign: "center" }}>
        ${Number(params.value).toFixed(2)}
      </Typography>
    ),
  },
  {
    field: "deltaRevenue",
    headerName: "Revenue Change",
    flex: 1.5,
    align: "center",
    headerAlign: "center",
    renderHeader: () => (
      <Tooltip title="Change in revenue compared to previous period (%)">
        <Typography sx={{ width: "100%", textAlign: "center" }}>
          Revenue Change (%)
        </Typography>
      </Tooltip>
    ),
    renderCell: (params: GridRenderCellParams<any>) => (
      <Typography
        sx={{ width: "100%", textAlign: "center" }}
        color={
          params.value > 0
            ? "success.main"
            : params.value < 0
              ? "error.main"
              : "textPrimary"
        }
      >
        {params.value > 0
          ? `+${Number(params.value).toFixed(2)}%`
          : `${Number(params.value).toFixed(2)}%`}
      </Typography>
    ),
  },
  {
    field: "avgCuisineFeedback",
    headerName: "Avg Cuisine Rating",
    flex: 1,
    align: "center",
    headerAlign: "center",
    renderCell: (params) => (
      <Typography sx={{ width: "100%", textAlign: "center" }}>
        {Number(params.value).toFixed(2)}
      </Typography>
    ),
  },
  {
    field: "deltaCuisineFeedback",
    headerName: "Cuisine Rating Change",
    flex: 1.5,
    align: "center",
    headerAlign: "center",
    renderHeader: () => (
      <Tooltip title="Change in cuisine rating compared to previous period (%)">
        <Typography sx={{ width: "100%", textAlign: "center" }}>
          Cuisine Rating Change (%)
        </Typography>
      </Tooltip>
    ),
    renderCell: (params: GridRenderCellParams<any>) => (
      <Typography
        sx={{ width: "100%", textAlign: "center" }}
        color={
          params.value > 0
            ? "success.main"
            : params.value < 0
              ? "error.main"
              : "textPrimary"
        }
      >
        {params.value > 0
          ? `+${Number(params.value).toFixed(2)}%`
          : `${Number(params.value).toFixed(2)}%`}
      </Typography>
    ),
  },
];

export default function ReportTable({
  rows,
  reportType,
}: {
  rows: any[];
  reportType: string;
}) {
  const columns =
    reportType === "staff_performance" ? staffPerformanceColumns : salesColumns;
  const getRowId = (row: any) =>
    reportType === "staff_performance"
      ? `${row.waiterName}-${row.locationId}`
      : row.locationId;

  return (
    <Box sx={{ height: 500, width: "100%", p: 2 }}>
      <DataGrid
        rows={rows}
        columns={columns}
        getRowId={getRowId}
        disableRowSelectionOnClick
        sx={{
          "& .MuiDataGrid-columnHeaders": {
            backgroundColor: "#e6f5e6",
            fontWeight: "bold",
          },
          "& .MuiDataGrid-cell": {
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            borderColor: "rgba(224, 224, 224, 1)",
          },
          "& .MuiDataGrid-columnHeader": {
            borderColor: "rgba(224, 224, 224, 1)",
          },
          "& .MuiDataGrid-cellContent": {
            width: "100%",
            textAlign: "center",
          },
        }}
      />
    </Box>
  );
}
