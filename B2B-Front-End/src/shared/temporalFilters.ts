export type TemporalFilterValue = "recent" | "today" | "7d" | "30d" | "custom";

export type TemporalRange = {
  from?: string;
  to?: string;
};

export const temporalFilterOptions: Array<{ value: TemporalFilterValue; label: string }> = [
  { value: "recent", label: "Recents" },
  { value: "today", label: "Aujourd'hui" },
  { value: "7d", label: "7 derniers jours" },
  { value: "30d", label: "30 derniers jours" },
  { value: "custom", label: "Periode personnalisee" },
];

export function resolveTemporalRange(
  filter: TemporalFilterValue,
  customFrom: string,
  customTo: string,
  now = new Date()
): TemporalRange {
  if (filter === "recent") return {};

  if (filter === "today") {
    const start = new Date(now);
    start.setHours(0, 0, 0, 0);
    return { from: start.toISOString(), to: now.toISOString() };
  }

  if (filter === "7d" || filter === "30d") {
    const start = new Date(now);
    start.setDate(start.getDate() - (filter === "7d" ? 7 : 30));
    return { from: start.toISOString(), to: now.toISOString() };
  }

  return {
    from: dateInputToIso(customFrom, false),
    to: dateInputToIso(customTo, true),
  };
}

function dateInputToIso(value: string, endOfDay: boolean) {
  if (!value) return undefined;
  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return undefined;
  if (endOfDay) {
    date.setHours(23, 59, 59, 999);
  }
  return date.toISOString();
}
