import React, { useId } from "react";

type ButtonVariant = "primary" | "secondary";

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
};

export function Button({
  variant = "primary",
  disabled,
  style,
  type,
  ...props
}: ButtonProps) {
  const id = useId().replace(/:/g, "");
  const className = `ui-btn-${id}`;

  const background = variant === "primary" ? "#111827" : "#ffffff";
  const color = variant === "primary" ? "#ffffff" : "#111827";
  const border = variant === "primary" ? "1px solid #111827" : "1px solid #d1d5db";

  return (
    <>
      <style>{`
        .${className} {
          appearance: none;
          border-radius: 10px;
          padding: 10px 14px;
          border: ${border};
          background: ${background};
          color: ${color};
          font-weight: 600;
          cursor: ${disabled ? "not-allowed" : "pointer"};
          opacity: ${disabled ? 0.6 : 1};
          outline: none;
          min-height: 40px;
          line-height: 1.2;
          touch-action: manipulation;
        }
        .${className}:focus-visible {
          box-shadow: 0 0 0 3px rgba(59,130,246,0.45);
        }
        .${className}:disabled {
          pointer-events: none;
        }
      `}</style>

      <button
        {...props}
        type={type ?? "button"}
        disabled={disabled}
        className={className}
        style={style}
      />
    </>
  );
}
