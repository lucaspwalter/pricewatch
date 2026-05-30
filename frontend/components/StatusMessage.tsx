export function StatusMessage({ message, type }: { message: string; type: "error" | "success" }) {
  return (
    <p
      className={`rounded-md px-3 py-2 text-sm ${
        type === "error"
          ? "border border-red-200 bg-red-50 text-red-700"
          : "border border-emerald-200 bg-emerald-50 text-emerald-700"
      }`}
    >
      {message}
    </p>
  );
}
