import React, { useState, useEffect } from "react";
import { useTheme } from "@mui/material";

type DateRange = [Date | null, Date | null];

interface CustomDateRangePickerProps {
  value: DateRange;
  onChange: (range: DateRange) => void;
}

const DAYS = ["M", "T", "W", "T", "F", "S", "S"];

function getMonthMatrix(month: number, year: number) {
  const firstDay = new Date(year, month, 1);
  const startDay = (firstDay.getDay() + 6) % 7; // Monday as first day
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const matrix: (number | null)[][] = [];
  let week: (number | null)[] = Array(startDay).fill(null);
  for (let d = 1; d <= daysInMonth; d++) {
    week.push(d);
    if (week.length === 7) {
      matrix.push(week);
      week = [];
    }
  }
  if (week.length) matrix.push([...week, ...Array(7 - week.length).fill(null)]);
  return matrix;
}

function isSameDay(a: Date | null, b: Date | null) {
  return (
    a &&
    b &&
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}

function isInRange(
  d: number | null,
  month: number,
  year: number,
  start: Date | null,
  end: Date | null
) {
  if (!d || !start || !end) return false;
  const date = new Date(year, month, d);
  return date >= start && date <= end;
}

export const CustomDateRangePicker: React.FC<CustomDateRangePickerProps> = ({
  value,
  onChange,
}) => {
  const theme = useTheme();
  const today = new Date();
  const [month, setMonth] = useState(today.getMonth());
  const [year, setYear] = useState(today.getFullYear());
  const [range, setRange] = useState<DateRange>(value);

  // Update internal range when external value changes
  useEffect(() => {
    setRange(value);
  }, [value]);

  const matrix = getMonthMatrix(month, year);

  const handleDayClick = (d: number | null) => {
    if (!d) return;
    const clicked = new Date(year, month, d);

    // If no start date is selected or both dates are selected
    if (!range[0] || (range[0] && range[1])) {
      setRange([clicked, null]);
      onChange([clicked, null]);
    }
    // If start date is selected but end date is not
    else {
      // If clicked date is before start date, make it the start date
      if (clicked < range[0]) {
        setRange([clicked, range[0]]);
        onChange([clicked, range[0]]);
      }
      // If clicked date is after start date, make it the end date
      else {
        setRange([range[0], clicked]);
        onChange([range[0], clicked]);
      }
    }
  };

  const prevMonth = () => {
    if (month === 0) {
      setMonth(11);
      setYear((y) => y - 1);
    } else {
      setMonth((m) => m - 1);
    }
  };

  const nextMonth = () => {
    if (month === 11) {
      setMonth(0);
      setYear((y) => y + 1);
    } else {
      setMonth((m) => m + 1);
    }
  };

  const isDarkMode = theme.palette.mode === "dark";
  const backgroundColor = isDarkMode ? theme.palette.background.paper : "#fff";
  const textColor = isDarkMode ? theme.palette.text.primary : "#000";
  const rangeBgColor = isDarkMode ? "rgba(0, 173, 12, 0.2)" : "#eaffea";
  const selectedBgColor = isDarkMode ? theme.palette.primary.main : "#00ad0c";
  const selectedTextColor = isDarkMode
    ? theme.palette.primary.contrastText
    : "#fff";
  const borderColor = isDarkMode ? theme.palette.divider : "#00ad0c44";

  return (
    <div className="calendar-container">
      <div className="calendar-header">
        <button type="button" onClick={prevMonth}>
          &lt;
        </button>
        <span>
          {new Date(year, month).toLocaleString("default", {
            month: "long",
            year: "numeric",
          })}
        </span>
        <button type="button" onClick={nextMonth}>
          &gt;
        </button>
      </div>
      <div className="calendar-grid">
        {DAYS.map((d) => (
          <div key={d} className="calendar-day-label">
            {d}
          </div>
        ))}
        {matrix.flat().map((d, i) => {
          const start = range[0];
          const end = range[1];
          const inRange = isInRange(d, month, year, start, end);
          const isStart = d && isSameDay(new Date(year, month, d), start);
          const isEnd = d && isSameDay(new Date(year, month, d), end);
          const isInFuture = d && new Date(year, month, d) > today;

          return (
            <div
              key={i}
              className={`calendar-day${d ? "" : " empty"}${
                inRange ? " in-range" : ""
              }${isStart ? " start" : ""}${isEnd ? " end" : ""}${
                isInFuture ? " future" : ""
              }`}
              onClick={() => !isInFuture && handleDayClick(d)}
            >
              {d || ""}
            </div>
          );
        })}
      </div>
      <style>{`
        .calendar-container {
          background: ${backgroundColor};
          border-radius: 18px;
          box-shadow: 0 0 0 3px ${borderColor};
          width: 320px;
          padding: 16px;
          color: ${textColor};
        }
        .calendar-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-weight: 600;
          font-size: 1.3rem;
          margin-bottom: 8px;
          color: ${textColor};
        }
        .calendar-header button {
          background: none;
          border: none;
          font-size: 1.5rem;
          cursor: pointer;
          color: ${textColor};
          padding: 4px 8px;
          border-radius: 4px;
          transition: background-color 0.2s;
        }
        .calendar-header button:hover {
          background-color: ${
            isDarkMode ? "rgba(255, 255, 255, 0.1)" : "rgba(0, 0, 0, 0.1)"
          };
        }
        .calendar-grid {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          gap: 4px;
        }
        .calendar-day-label {
          text-align: center;
          color: ${isDarkMode ? theme.palette.text.secondary : "#888"};
          font-weight: 500;
        }
        .calendar-day {
          text-align: center;
          padding: 8px 0;
          border-radius: 50%;
          cursor: pointer;
          font-weight: 500;
          font-size: 1.1rem;
          transition: all 0.2s;
          color: ${textColor};
        }
        .calendar-day:hover:not(.empty):not(.future) {
          background-color: ${
            isDarkMode ? "rgba(255, 255, 255, 0.1)" : "rgba(0, 0, 0, 0.1)"
          };
        }
        .calendar-day.empty {
          background: none;
          cursor: default;
        }
        .calendar-day.in-range {
          background: ${rangeBgColor};
          color: ${selectedBgColor};
          border-radius: 18px;
        }
        .calendar-day.start,
        .calendar-day.end {
          background: ${selectedBgColor};
          color: ${selectedTextColor};
          border-radius: 50%;
        }
        .calendar-day.future {
          opacity: 0.5;
          cursor: not-allowed;
        }
      `}</style>
    </div>
  );
};
