import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "./useAuth";

type Props = {
  children: React.ReactNode;
};

export default function RequireAuth({ children }: Props) {
  const auth = useAuth();
  const location = useLocation();

  if (auth.status === "idle" || auth.status === "loading") {
    return <div style={{ padding: 24 }}>Loading...</div>;
  }

  if (!auth.isAuthenticated) {
    const returnTo = location.pathname + location.search + location.hash;
    return <Navigate to={`/login?returnTo=${encodeURIComponent(returnTo)}`} replace />;
  }

  return <>{children}</>;
}
