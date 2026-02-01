import { createBrowserRouter, Navigate } from "react-router-dom";
import AuthPage from "./pages/AuthPage";
import FeedPage from "./pages/FeedPage";
import WallPage from "./pages/WallPage";

export const router = createBrowserRouter([
  { path: "/", element: <Navigate to="/feed" replace /> },
  { path: "/auth", element: <AuthPage /> },
  { path: "/feed", element: <FeedPage /> },
  { path: "/wall", element: <WallPage /> },
]);