import React from "react";
import { Text, View, StyleSheet } from "react-native";

type Props = { isOnline: boolean };

export const OfflineBanner = ({ isOnline }: Props) => {
  if (isOnline) return null;
  return (
    <View style={styles.banner}>
      <Text style={styles.text}>{"📡  Offline — using cached data"}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  banner: {
    backgroundColor: "#E65100",
    paddingVertical: 6,
    paddingHorizontal: 12,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
  },
  text: {
    color: "#fff",
    fontWeight: "700",
    fontSize: 13,
    textAlign: "center",
  },
});