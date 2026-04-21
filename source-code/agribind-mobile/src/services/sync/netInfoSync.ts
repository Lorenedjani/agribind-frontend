import NetInfo from "@react-native-community/netinfo";
import { store } from "../../store";
import { setOnlineStatus } from "../../store/slices/appSlice";

export const attachConnectivityListener = () => {
  return NetInfo.addEventListener((state) => {
    store.dispatch(setOnlineStatus(Boolean(state.isConnected)));
  });
};
