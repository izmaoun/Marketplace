import { redirect, type LoaderFunction } from "react-router";
import { getAccessToken, hasRole, refreshTokens, type UserRole } from "./auth";

export function requireRole(role: UserRole): LoaderFunction {
  return async () => {
    if (!getAccessToken() && !(await refreshTokens())) {
      return redirect("/sign-in");
    }

    if (!hasRole(role) && !(await refreshTokens())) {
      return redirect("/sign-in");
    }

    if (!hasRole(role)) {
      return redirect("/sign-in");
    }

    return null;
  };
}
