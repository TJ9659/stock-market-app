export const formatDescription = (str : string) => {
  if (!str) return "";
  return str.length > 15 ? str.substring(0, 20) + "..." : str;
};