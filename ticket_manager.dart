import 'package:image_picker/image_picker.dart';
import 'package:tesseract_ocr/tesseract_ocr.dart';

class TicketManager {
  Future<String> scanTicket() async {
    final picker = ImagePicker();
    final pickedImage = await picker.pickImage(source: ImageSource.camera);
    if (pickedImage!= null) {
      final text = await TesseractOcr.extractText(pickedImage.path);
      return text;
    } else {
      return '';
    }
  }
}