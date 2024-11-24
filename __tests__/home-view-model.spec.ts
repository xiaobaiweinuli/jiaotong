import { HomeViewModel } from '../app/views/home/home-view-model';

describe('HomeViewModel', () => {
  let viewModel: HomeViewModel;

  beforeEach(() => {
    viewModel = new HomeViewModel();
  });

  describe('calculateDistance', () => {
    it('should calculate distance between two points', () => {
      // Tokyo Station coordinates
      const lat1 = 35.6812;
      const lon1 = 139.7671;
      
      // Shinjuku Station coordinates
      const lat2 = 35.6896;
      const lon2 = 139.7006;
      
      // @ts-ignore - accessing private method for testing
      const distance = viewModel.calculateDistance(lat1, lon1, lat2, lon2);
      
      // The actual distance is approximately 6.4 km
      expect(distance).toBeCloseTo(6.4, 1);
    });
  });

  describe('fromLocation', () => {
    it('should update fromLocation value', () => {
      const newLocation = 'Tokyo Station';
      viewModel.fromLocation = newLocation;
      expect(viewModel.fromLocation).toBe(newLocation);
    });
  });
});