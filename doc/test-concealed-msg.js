const BigInteger = require('jsbn').BigInteger;
const BlindSignature = require('blind-signatures');
const NodeRSA = require('node-rsa');
const prompt = require('prompt-sync')();

const publicKeyInput = prompt("Public key in PEM?");
const publicKey = new NodeRSA(publicKeyInput);

// Concealing message
const message = "The quick brown fox jumps over the lazy dog! Hello World!";
const concealingResult = BlindSignature.blind(
    {
        message: message,
        N: publicKey.keyPair.n.toString(),
        E: publicKey.keyPair.e.toString(),
    }
);

const blindedHexStr = concealingResult.blinded.toString(16);
const blindedBuffer = Buffer.from(blindedHexStr, 'hex');

console.log(`\nConcealed message (base64): ${blindedBuffer.toString('base64')}`);
console.log(`\nr: ${concealingResult.r.toString(16)}`);

// Getting signature on concealed message and producing revealed signature
const signatureOnConcealedMessageBase64 = prompt("Signature on concealed message (base64)?");
const signatureOnConcealedMessageBuffer = Buffer.from(signatureOnConcealedMessageBase64, 'base64');
const signatureOnConcealedMessageHex = signatureOnConcealedMessageBuffer.toString('hex');
const signatureOnConcealedMessage = new BigInteger(signatureOnConcealedMessageHex, 16)

const signatureOnRevealedMessage = BlindSignature.unblind({
    signed: signatureOnConcealedMessage,
    N: publicKey.keyPair.n.toString(),
    r: concealingResult.r,
});

const revealedMessageBuffer = Buffer.from(message, 'utf8');
const revealedMessageBase64 = revealedMessageBuffer.toString('base64');
console.log(`\nRevealed message (base64): ${revealedMessageBase64}`);

const signatureOnRevealedMessageHex = signatureOnRevealedMessage.toString(16);
const signatureOnRevealedMessageBuffer = Buffer.from(signatureOnRevealedMessageHex, 'hex');
const signatureOnRevealedMessageBase64 = signatureOnRevealedMessageBuffer.toString('base64');
console.log(`\nSignature on revealed message (base64): ${signatureOnRevealedMessageBase64}`);


const result = BlindSignature.verify({
    unblinded: signatureOnRevealedMessage,
    N: publicKey.keyPair.n.toString(),
    E: publicKey.keyPair.e.toString(),
    message: message,
  });
  if (result) {
    console.log('Alice: Signatures verify!');
  } else {
    console.log('Alice: Invalid signature');
  }


// TODO: Read public key from stdin
// TODO: Generate blinded message

