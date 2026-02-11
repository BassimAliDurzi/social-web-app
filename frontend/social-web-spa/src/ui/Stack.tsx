import React from "react";

type StackProps = {
  as?: React.ElementType;
  gap?: number;
  align?: React.CSSProperties["alignItems"];
  justify?: React.CSSProperties["justifyContent"];
  wrap?: React.CSSProperties["flexWrap"];
  style?: React.CSSProperties;
  children: React.ReactNode;
};

export function Stack({
  as: Component = "div",
  gap = 12,
  align,
  justify,
  wrap,
  style,
  children,
}: StackProps) {
  return (
    <Component
      style={{
        display: "flex",
        flexDirection: "column",
        gap,
        alignItems: align,
        justifyContent: justify,
        flexWrap: wrap,
        ...style,
      }}
    >
      {children}
    </Component>
  );
}
