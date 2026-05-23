const StatRow = ({
  label,
  value,
  icon,
}: {
  label: string;
  value: string | React.ReactNode;
  icon: React.ReactNode;
}) => (
  <div className="flex justify-between items-center group">
    <div className="flex items-center gap-2 text-gray-500 group-hover:text-gray-400 transition-colors">
      {icon}
      <span className="text-[11px] font-bold uppercase tracking-tight">
        {label}
      </span>
    </div>
    <span className="font-mono text-sm text-gray-100 text-right">
      {value || "N/A"}
    </span>
  </div>
);

export default StatRow;
