from flask import Flask, request, jsonify
import pickle
import sklearn
print(sklearn.__version__)
import numpy as np
from flask_cors import CORS
import logging

# Initialize logging
logging.basicConfig(level=logging.INFO)

# Load the trained model and scaler
try:
    with open('xgb_classifier_model.pkl', 'rb') as model_file:
        model = pickle.load(model_file)
    logging.info("Model loaded successfully.")
except Exception as e:
    logging.error("Failed to load model: %s", str(e))
    raise

try:
    with open('scaler.pkl', 'rb') as scaler_file:
        scaler = pickle.load(scaler_file)
    logging.info("Scaler loaded successfully.")
except Exception as e:
    logging.error("Failed to load scaler: %s", str(e))
    raise

# Initialize Flask app
app = Flask(__name__)
CORS(app)

@app.route('/predict', methods=['POST'])
def predict():
    try:
        # Get the JSON data from the request
        data = request.get_json()

        # Check if 'features' key exists in the received data
        if 'features' not in data:
            return jsonify({'error': 'No features provided'}), 400
        
        # Extract and reshape features for model prediction
        features = np.array(data['features']).reshape(1, -1)
        logging.info("Received features: %s", features)
        
        # Scale the features
        scaled_features = scaler.transform(features)

        # Make prediction
        prediction = model.predict(scaled_features)[0]
        
        # Map prediction to a string
        prediction_str = "Normal Transaction" if prediction == 0 else "Fraud Transaction"
        
        # Return the prediction as a JSON response
        return jsonify({'prediction': prediction_str})

    except Exception as e:
        # Catch and log any exceptions that occur during the process
        logging.error("Error in prediction: %s", str(e))
        return jsonify({'error': str(e)}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
