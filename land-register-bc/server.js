import express from "express";
import bodyParser from "body-parser";
import { create } from "ipfs-http-client";
import dotenv from "dotenv";
import findConfig from "find-config";
import { JsonRpcProvider, Wallet, Contract } from "ethers";
import { readFileSync } from "fs";
import axios from "axios";
import multer from "multer";

// Load environment variables
dotenv.config({ path: findConfig(".env") });
console.log("Environment variables loaded.");

console.log("Initializing IPFS...");
const ipfs = create({ host: "localhost", port: "5001", protocol: "http" });

// Load the smart contract JSON file for ABI
const contractABI = JSON.parse(
	readFileSync("./build/contracts/LandTransaction.json", "utf8")
);

// Initialize Express app
const app = express();
app.use(bodyParser.json());

// Ganache RPC URL and account details
const providerURL = process.env.GANACHE_URL;
const privateKey = process.env.CONTRACT_PRIVATE_KEY;
const contractAddress = process.env.CONTRACT_ADDRESS;

console.log("Using provider URL:", providerURL);
console.log("Using contract address:", contractAddress); // Trace contract address

// Set up a provider and wallet
const provider = new JsonRpcProvider(providerURL);
const wallet = new Wallet(privateKey, provider);

// Initialize the contract instance
const contract = new Contract(contractAddress, contractABI.abi, wallet);

// Function to calculate total ether and average time between transactions
async function calculateTransactionMetrics() {
	const sendTransactions = await getSendTransactions(wallet.address);
	const receiveTransactions = await getReceiveTransactions(wallet.address);

	let totalEtherSent = 0;
	let totalEtherReceived = 0;
	let sendTimestamps = [];
	let receiveTimestamps = [];

	// Calculate total ether and collect timestamps for sent transactions
	for (const tx of sendTransactions) {
		totalEtherSent += parseFloat(tx.value); // Assuming value is in ether

		// Convert block timestamp to JavaScript Date object
		const block = await provider.getBlock(tx.blockNumber);
		const timestamp = new Date(block.timestamp * 1000); // Convert Unix timestamp to JavaScript date
		sendTimestamps.push(timestamp);
	}

	// Calculate total ether and collect timestamps for received transactions
	for (const tx of receiveTransactions) {
		totalEtherReceived += parseFloat(tx.value);

		// Convert block timestamp to JavaScript Date object
		const block = await provider.getBlock(tx.blockNumber);
		const timestamp = new Date(block.timestamp * 1000); // Convert Unix timestamp to JavaScript date
		receiveTimestamps.push(timestamp);
	}

	// Calculate average times
	const averageTimeBetweenSend =
		sendTimestamps.length > 0 ? calculateAverageTime(sendTimestamps) : 0;
	const averageTimeBetweenReceive =
		receiveTimestamps.length > 0 ? calculateAverageTime(receiveTimestamps) : 0;

	// Calculate total number of transactions
	const totalTransactions =
		sendTransactions.length + receiveTransactions.length;

	// Get Ether balance of the wallet
	const totalEtherBalance = await getEtherBalance(wallet.address); // Assuming you have a getEtherBalance function

	// transaction send to contract
	const minValueSendToContract = 0;
	const maxValueSendToContract = 0;
	const avgValueSendToContract = 0;

	return {
		sendTransactions,
		receiveTransactions,
		totalTransactions,
		averageTimeBetweenSend,
		averageTimeBetweenReceive,
		totalEtherSent,
		totalEtherReceived,
		totalEtherBalance,
		minValueSendToContract,
		maxValueSendToContract,
		avgValueSendToContract,
	};
}

// Function to calculate average time between timestamps
function calculateAverageTime(timestamps) {
	if (timestamps.length < 2) return 0;

	const differences = [];

	// Sort timestamps to ensure they are in chronological order
	timestamps.sort((a, b) => a - b);

	for (let i = 1; i < timestamps.length; i++) {
		const diff = (timestamps[i] - timestamps[i - 1]) / 60000; // difference in minutes
		differences.push(diff);
	}

	// Return the average of time differences
	return differences.reduce((acc, curr) => acc + curr, 0) / differences.length;
}

// Conversion Constants
const WEI_PER_ETHER = BigInt(10 ** 18);
const ETHER_TO_INR = 1000000; // Assuming 1 ether = 1,000,000 INR

// Function to convert INR to Wei
function inrToWei(inr) {
	console.log("INR input: " + inr);

	const etherAmount = inr / ETHER_TO_INR; // Keep it as a float
	console.log("Converted Ether Amount (float): " + etherAmount);

	const weiAmount = BigInt(Math.floor(etherAmount * Number(WEI_PER_ETHER))); // Convert to BigInt after calculating wei
	console.log("Converted Wei Amount (BigInt): " + weiAmount);

	return weiAmount;
}

// Handle POST request for creating a transaction
app.post("/make-transaction", async (req, res) => {
	const data = req.body;
	console.log("Received request to create transaction:", data);

	try {
		// Calculate transaction metrics
		const metrics = await calculateTransactionMetrics();

		// Prepare data for AI service
		const aiData = {
			averageTimeBetweenSend: metrics.averageTimeBetweenSend,
			averageTimeBetweenReceive: metrics.averageTimeBetweenReceive,
			sendTransactions: metrics.sendTransactions.length,
			receiveTransactions: metrics.receiveTransactions.length,
			minValueSendToContract: metrics.minValueSendToContract,
			maxValueSendToContract: metrics.maxValueSendToContract,
			avgValueSendToContract: metrics.avgValueSendToContract,
			totalTransactions: metrics.totalTransactions,
			totalEtherSent: metrics.totalEtherSent,
			totalEtherReceived: metrics.totalEtherReceived,
			totalEtherBalance: metrics.totalEtherBalance,
		};

		const aiDataArray = [
			aiData.averageTimeBetweenSend,
			aiData.averageTimeBetweenReceive,
			aiData.sendTransactions,
			aiData.receiveTransactions,
			aiData.minValueSendToContract,
			aiData.maxValueSendToContract,
			aiData.avgValueSendToContract,
			aiData.totalTransactions,
			aiData.totalEtherSent,
			aiData.totalEtherReceived,
			aiData.totalEtherBalance,
		];

		// Convert totalEtherBalance to a number if needed
		if (typeof aiData.totalEtherBalance === "string") {
			aiDataArray[10] = Number(aiData.totalEtherBalance);
		}

		const jsonData = { features: aiDataArray };
		console.log(JSON.stringify(jsonData));

		// Send data to AI service without double-stringifying
		const aiResponse = await sendDataToAI(jsonData);
		const response = JSON.stringify(aiResponse, null, 2);
		console.log(response);

		// Check response from AI service
		if (aiResponse.error) {
			return res
				.status(400)
				.json({ error: "AI service returned an error: " + aiResponse.error });
		}

		// Validate the AI prediction
		if (aiResponse.prediction === "Fraud Transaction") {
			console.log("Transaction flagged as fraudulent.");
			//   return res.status(403).json({ error: 'Transaction flagged as fraudulent by AI service.' });
		}

		// Add the JSON data to IPFS
		const result = await ipfs.add(Buffer.from(JSON.stringify(data)));
		const ipfsHash = result.path;
		console.log("IPFS hash generated:", ipfsHash);

		const {
			area,
			city,
			state,
			propertyId,
			physicalSurveyNo,
			price,
			sellerName,
			sellerAddress,
			buyerName,
			buyerAddress,
		} = data;

		// Validate input
		if (
			!buyerName ||
			!sellerName ||
			!propertyId ||
			!price ||
			!buyerAddress ||
			!sellerAddress ||
			!area ||
			!city ||
			!state ||
			!physicalSurveyNo
		) {
			console.log("Validation failed: Missing required fields.");
			return res.status(400).json({ error: "Missing required fields" });
		}
		// Convert price from INR to Wei
		const priceInRupees = parseFloat(price);
		console.log("Price in Rupees:", priceInRupees);

		const priceInWei = inrToWei(priceInRupees);

		// Get the current nonce
		let nonce = await provider.getTransactionCount(wallet.address);

		// Make payment to the seller
		console.log("Making payment to seller...");
		const paymentTx = await wallet.sendTransaction({
			to: sellerAddress,
			value: priceInWei,
			nonce: nonce, // Set the nonce for the payment transaction
		});

		// Wait for the payment transaction to be mined
		await paymentTx.wait();
		console.log("Payment successful:", paymentTx);

		// Call the smart contract function to create a new transaction
		console.log("Calling smart contract to making transaction...");
		console.log("Transaction sent! Waiting for confirmation...");
		console.log("Transaction confirmed! Transaction ID:", paymentTx.hash);

		// Return the transaction details
		res.status(200).json({
			message: "Paid",
			paymentHash: paymentTx.hash, // return paymentHash
			ipfsHash: ipfsHash,
		});
	} catch (error) {
		console.error("Error creating transaction:", error);
		res
			.status(500)
			.json({ error: "Transaction creation failed. " + error.message });
	}
});

// send the data to AI
async function sendDataToAI(data) {
	try {
		const response = await axios.post(process.env.AI_ENDPOINT, data, {
			headers: { "Content-Type": "application/json" },
		});
		return response.data;
	} catch (error) {
		console.error("Error calling AI service:", error);
		return { error: error.message };
	}
}

// getSendTransactions
async function getSendTransactions(walletAddress) {
	const sendTransactions = [];
	const latestBlock = await provider.getBlockNumber(); // Get the latest block number

	// Loop through the last 100 blocks (you can adjust this as needed)
	const numBlocksToCheck = 100; // Number of blocks to check
	for (let i = latestBlock; i >= latestBlock - numBlocksToCheck; i--) {
		const block = await provider.getBlock(i); // Get block without transactions

		// Iterate through transaction hashes in the block
		for (const txHash of block.transactions) {
			const transaction = await provider.getTransaction(txHash); // Get the transaction details
			if (transaction.from.toLowerCase() === walletAddress.toLowerCase()) {
				// Transaction sent from this wallet
				sendTransactions.push(transaction);
			}
		}
	}

	return sendTransactions; // Return an array of send transactions
}

// getReceiveTransactions
async function getReceiveTransactions(walletAddress) {
	const receiveTransactions = [];
	const latestBlock = await provider.getBlockNumber(); // Get the latest block number

	// Loop through the last 100 blocks (you can adjust this as needed)
	const numBlocksToCheck = 100; // Number of blocks to check
	for (let i = latestBlock; i >= latestBlock - numBlocksToCheck; i--) {
		const block = await provider.getBlock(i); // Get block without transactions

		// Iterate through transaction hashes in the block
		for (const txHash of block.transactions) {
			const transaction = await provider.getTransaction(txHash); // Get the transaction details
			if (
				transaction.to &&
				transaction.to.toLowerCase() === walletAddress.toLowerCase()
			) {
				// Transaction received by this wallet
				receiveTransactions.push(transaction);
			}
		}
	}

	return receiveTransactions; // Return an array of receive transactions
}

// Function to manually convert Wei to Ether
function convertWeiToEther(weiValue) {
	const etherValue = parseFloat(weiValue) / Math.pow(10, 18); // 1 Ether = 10^18 Wei
	return etherValue.toFixed(6); // Returning with 6 decimal places (you can adjust as needed)
}

// Fetch Ether balance of the wallet
async function getEtherBalance(address) {
	const balance = await provider.getBalance(address); // Balance is returned in Wei
	return convertWeiToEther(balance); // Manually convert Wei to Ether
}

// Configure multer for file storage
const storage = multer.memoryStorage();
const upload = multer({ storage: storage });

// upload file to ipfs
app.post("/file", upload.single("fileData"), async (req, res) => {
	const fileData = req.file; // This will be your uploaded file
	const fileName = req.body.fileName; // This will be your file name

	console.log(`fileData: ${fileData}`);

	// Check if a file was uploaded
	if (!fileData) {
		return res.status(400).json({ error: "No file uploaded." });
	}

	try {
		// Get the file buffer from the request
		const fileBuffer = fileData.buffer;
		console.log(`fileBuffer: ${fileBuffer}`);
		console.log(`fileName: ${fileName}`);

		if (fileBuffer.length === 0) {
			return res.status(400).json({ error: "File buffer is empty." });
		}

		// Add the file to IPFS
		const result = await ipfs.add(fileBuffer);
		const ipfsHash = result.path;
		console.log("IPFS hash generated:", ipfsHash);
		console.log("File uploaded to IPFS successfully.");

		// Send the IPFS hash back as a response
		res.status(200).json({ ipfsHash: result.path });
	} catch (err) {
		console.log("Error: " + err);
		if (String(err).includes("ECONNREFUSED")) {
			return res.status(422).json({
				message: "IPFS is not Running / Check Port No",
				errors: err,
			});
		} else if (String(err).includes("noNetwork")) {
			return res.status(422).json({
				message: "Ganache is not Running / Contract Address not valid",
				errors: err,
			});
		} else {
			console.error(err);
			res.status(500).json({ error: "Failed to upload file to IPFS" });
		}
	}
});

// Route to retrieve a file from IPFS
app.get("/api/ipfs/:fileHash", async (req, res) => {
	const { fileHash } = req.params;

	console.log("Getting file from IPF....");
	console.log(`fileHash: ${fileHash}`);

	try {
		// Fetch file data from IPFS
		const fileBuffer = [];
		for await (const chunk of ipfs.cat(fileHash)) {
			console.log(`Received chunk: ${chunk}`);
			fileBuffer.push(chunk);
		}
		console.log(`fileBuffer: ${fileBuffer}`);

		const fileData = Buffer.concat(fileBuffer);
		// Check if the file data was retrieved successfully
		if (fileData.length === 0) {
			console.error("No data retrieved for the specified file hash.");
			return res.status(404).send("File not found");
		}
		console.log(`fileData: ${fileData}`);

		// Send the file data as a response
		res.setHeader("Content-Type", "application/pdf");
		res.send(fileData);
	} catch (error) {
		console.error("Error fetching file from IPFS:", error);
		res.status(500).json({ error: "Failed to retrieve file from IPFS" });
	}
});

// Start the server
app.listen(3000, () => {
	console.log("Server is running on port 3000");
});
