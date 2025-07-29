// store.ts
import { configureStore, combineReducers } from '@reduxjs/toolkit';
import { persistStore, persistReducer, FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER } from 'redux-persist';
import storage from 'redux-persist/lib/storage';
import cartReducer from '../redux/cartSlice';

const rootReducer = combineReducers({
  cart: cartReducer,
  // other reducers...
});

const persistConfig = {
  key: 'root',
  version: 1,
  storage,
  whitelist: ['cart'],
  debug: true // Enable debugging
};

const persistedReducer = persistReducer(persistConfig, rootReducer);

export const store = configureStore({
  reducer: persistedReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
      },
    }),
  devTools: process.env.NODE_ENV !== 'production',
});

export const persistor = persistStore(store);

// Optional: Log the initial state
console.log('Initial State:', store.getState());

// Optional: Subscribe to store changes
store.subscribe(() => {
  console.log('Store Updated:', store.getState());
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;