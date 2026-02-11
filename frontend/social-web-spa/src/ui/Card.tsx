import React from "react";

type CardProps = {
  style?: React.CSSProperties;
  children: React.ReactNode;
};

export function Card({ style, children }: CardProps) {
  return (
    <div
      style={{
        border: "1px solid #80f7f1",
        borderRadius: 12,
        background: "#e8faf9",
        padding: 14,
        ...style,
      }}
    >
      {children}
    </div>
  );
}
