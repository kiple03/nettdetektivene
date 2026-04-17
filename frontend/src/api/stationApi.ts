import type {StationData} from "@/types/station.ts";
import {newsMock} from "@/mock/newsMock.ts";

export async function fetchStationById(id: number): Promise<StationData> {
  // TODO: replace mock with backend call when API is ready
  return Promise.resolve(newsMock);
}